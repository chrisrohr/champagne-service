package org.kiwiproject.champagne.resource;

import static java.util.Objects.isNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.kiwiproject.champagne.util.DeployableSystems.getSystemIdOrThrowBadRequest;
import static org.kiwiproject.jaxrs.KiwiStandardResponses.standardNotFoundResponse;
import static org.kiwiproject.search.KiwiSearching.zeroBasedOffset;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.google.common.annotations.VisibleForTesting;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.DeploymentEnvironmentDao;
import org.kiwiproject.champagne.dao.ReleaseDao;
import org.kiwiproject.champagne.dao.ReleaseStatusDao;
import org.kiwiproject.champagne.dao.TaskDao;
import org.kiwiproject.champagne.dao.TaskStatusDao;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.model.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.model.manualdeployment.Release;
import org.kiwiproject.champagne.model.manualdeployment.ReleaseStage;
import org.kiwiproject.champagne.model.manualdeployment.ReleaseStatus;
import org.kiwiproject.champagne.model.manualdeployment.Task;
import org.kiwiproject.champagne.model.manualdeployment.TaskStatus;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;
import org.kiwiproject.jaxrs.exception.JaxrsNotFoundException;
import org.kiwiproject.spring.data.KiwiPage;

import java.util.List;
import java.util.Set;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/manual/deployment/tasks")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class TaskResource extends AuditableResource {

    private final ReleaseDao releaseDao;
    private final ReleaseStatusDao releaseStatusDao;
    private final TaskDao taskDao;
    private final TaskStatusDao taskStatusDao;
    private final DeploymentEnvironmentDao deploymentEnvironmentDao;

    public TaskResource (ReleaseDao releaseDao,
                         ReleaseStatusDao releaseStatusDao,
                         TaskDao taskDao,
                         TaskStatusDao taskStatusDao,
                         DeploymentEnvironmentDao deploymentEnvironmentDao,
                         AuditRecordDao auditRecordDao,
                         ApplicationErrorDao errorDao) {

        super(auditRecordDao, errorDao);

        this.releaseDao = releaseDao;
        this.releaseStatusDao = releaseStatusDao;
        this.taskDao = taskDao;
        this.taskStatusDao = taskStatusDao;
        this.deploymentEnvironmentDao = deploymentEnvironmentDao;
    }

    @GET
    @Path("/releases")
    @Timed
    @ExceptionMetered
    public Response getPagedReleases(@QueryParam("pageNumber") @DefaultValue("1") int pageNumber,
                                     @QueryParam("pageSize") @DefaultValue("50") int pageSize) {

        var systemId = getSystemIdOrThrowBadRequest();
        var releases = releaseDao.findPagedReleases(zeroBasedOffset(pageNumber, pageSize), pageSize, systemId);
        var releasesWithStatus = releases.stream()
                .map(this::buildReleaseWithStatusFrom)
                .toList();
        
        var totalCount = releaseDao.countReleases(systemId);
        return Response.ok(KiwiPage.of(pageNumber, pageSize, totalCount, releasesWithStatus).usingOneAsFirstPage()).build();
    }

    private ReleaseWithStatus buildReleaseWithStatusFrom(Release release) {
        var statuses = releaseStatusDao.findByReleaseId(release.getId());
        var environmentStatus = statuses.stream().collect(toMap(ReleaseStatus::getEnvironmentId, identity()));

        return ReleaseWithStatus.builder()
            .release(release)
            .environmentStatus(environmentStatus)
            .build();
    }

    @GET
    @Path("/releases/{releaseId}")
    @Timed
    @ExceptionMetered
    public Response getTasksForRelease(@PathParam("releaseId") long releaseId) {
        var tasks = taskDao.findByReleaseId(releaseId);

        var tasksWithStatus = tasks.stream()
                .map(this::buildTaskWithStatusFrom)
                .toList();

        return Response.ok(tasksWithStatus).build();
    }

    private TaskWithStatus buildTaskWithStatusFrom(Task task) {
        var statuses = taskStatusDao.findByTaskId(task.getId());
        var environmentStatus = statuses.stream().collect(toMap(TaskStatus::getEnvironmentId, identity()));

        return TaskWithStatus.builder()
            .task(task)
            .environmentStatus(environmentStatus)
            .build();
    }
    
    @POST
    @Path("/releases")
    @Timed
    @ExceptionMetered
    public Response addNewRelease(@Valid @NotNull Release release) {
        var systemId = release.getDeployableSystemId();

        if (isNull(release.getDeployableSystemId())) {
            systemId = getSystemIdOrThrowBadRequest();
            release = release.withDeployableSystemId(systemId);
        }

        var releaseId = releaseDao.insertRelease(release);
        auditAction(releaseId, Release.class, Action.CREATED);

        deploymentEnvironmentDao.findAllEnvironments(systemId).forEach(env -> {
            var status = ReleaseStatus.builder()
                .releaseId(releaseId)
                .environmentId(env.getId())
                .status(DeploymentTaskStatus.PENDING)
                .build(); 

            var releaseStatusId = releaseStatusDao.insertReleaseStatus(status);
            auditAction(releaseStatusId, ReleaseStatus.class, Action.CREATED);
        });

        return Response.accepted().build();
    }

    @POST
    @Timed
    @ExceptionMetered
    public Response addNewTask(@Valid @NotNull Task task) {
        var systemId = getSystemIdOrThrowBadRequest();
        var taskId = taskDao.insertTask(task);
        auditAction(taskId, Task.class, Action.CREATED);

        deploymentEnvironmentDao.findAllEnvironments(systemId).forEach(env -> {
            var status = TaskStatus.builder()
                .taskId(taskId)
                .environmentId(env.getId())
                .status(DeploymentTaskStatus.PENDING)
                .build(); 

            var taskStatusId = taskStatusDao.insertTaskStatus(status);
            auditAction(taskStatusId, TaskStatus.class, Action.CREATED);
        });

        calculateReleaseStatus(task.getReleaseId());

        return Response.accepted().build();
    }

    @VisibleForTesting
    void calculateReleaseStatus(long releaseId) {
        var tasksForRelease = taskDao.findByReleaseId(releaseId);

        var taskToStatusMap = tasksForRelease.stream()
                .map(task -> taskStatusDao.findByTaskId(task.getId()))
                .flatMap(List::stream)
                .collect(groupingBy(TaskStatus::getEnvironmentId));

        var releaseStatuses = releaseStatusDao.findByReleaseId(releaseId);

        releaseStatuses.forEach(status -> {
            var env = status.getEnvironmentId();
            var newStatus = rollupTaskStatus(status.getStatus(), taskToStatusMap.getOrDefault(env, List.of()));

            if (status.getStatus() != newStatus) {
                releaseStatusDao.updateStatus(status.getId(), newStatus);
                auditAction(status.getId(), ReleaseStatus.class, Action.UPDATED);
            }
        });
    }

    private static DeploymentTaskStatus rollupTaskStatus(DeploymentTaskStatus originalStatus, List<TaskStatus> taskStatuses) {
        var statuses = taskStatuses.stream()
                .map(TaskStatus::getStatus)
                .collect(toSet());

        // No tasks, keep original status
        if (statuses.isEmpty()) {
            return originalStatus;
        }

        // All tasks complete, set to complete
        if (allTasksWithStatus(statuses, DeploymentTaskStatus.COMPLETE)) {
            return DeploymentTaskStatus.COMPLETE;
        }

        // All tasks are not required
        if (allTasksWithStatus(statuses, DeploymentTaskStatus.NOT_REQUIRED)) {
            return DeploymentTaskStatus.NOT_REQUIRED;
        }

        // Any pending tasks
        if (anyTasksWithPendingStatus(statuses)) {
            return DeploymentTaskStatus.PENDING;
        }

        // There are at least one complete and one not required
        return DeploymentTaskStatus.COMPLETE;
    }

    private static boolean allTasksWithStatus(Set<DeploymentTaskStatus> statuses, DeploymentTaskStatus statusToCheck) {
        return statuses.stream().allMatch(status -> status == statusToCheck);
    }

    private static boolean anyTasksWithPendingStatus(Set<DeploymentTaskStatus> statuses) {
        return statuses.stream().anyMatch(status -> status == DeploymentTaskStatus.PENDING);
    }

    @PUT
    @Path("/releases/{statusId}/{status}")
    @Timed
    @ExceptionMetered
    public Response updateReleaseStatus(@PathParam("statusId") long statusId, 
                                        @PathParam("status") DeploymentTaskStatus status) {
        var updatedCount = releaseStatusDao.updateStatus(statusId, status);

        if (updatedCount == 0) {
            return standardNotFoundResponse("Unable to update release status with id" + statusId);
        } else {
            auditAction(statusId, ReleaseStatus.class, Action.UPDATED);
        }

        return Response.accepted().build();
    }

    @PUT
    @Path("/{statusId}/{status}")
    @Timed
    @ExceptionMetered
    public Response updateTaskStatus(@PathParam("statusId") long statusId, 
                                        @PathParam("status") DeploymentTaskStatus status) {
        var updatedCount = taskStatusDao.updateStatus(statusId, status);

        if (updatedCount == 0) {
            return standardNotFoundResponse("Unable to update task status with id" + statusId);
        } else {
            auditAction(statusId, TaskStatus.class, Action.UPDATED);
        }

        taskDao.findByTaskStatusId(statusId).ifPresent(task -> calculateReleaseStatus(task.getReleaseId()));
        return Response.accepted().build();
    }

    @DELETE
    @Path("/releases/{releaseId}")
    @Timed
    @ExceptionMetered
    public Response deleteRelease(@PathParam("releaseId") long releaseId) {
        releaseDao.deleteById(releaseId);

        auditAction(releaseId, Release.class, Action.DELETED);

        return Response.accepted().build();
    }

    @DELETE
    @Path("/{taskId}")
    @Timed
    @ExceptionMetered
    public Response deleteTask(@PathParam("taskId") long taskId) {
        var releaseId = taskDao.findById(taskId).map(Task::getReleaseId)
            .orElseThrow(() -> new JaxrsNotFoundException("Unable to find task with id" + taskId));

        taskDao.deleteById(taskId);
        auditAction(taskId, Task.class, Action.DELETED);

        calculateReleaseStatus(releaseId);

        return Response.accepted().build();
    }

    @GET
    @Path("/stages")
    @Timed
    @ExceptionMetered
    public Response getReleaseStages() {
        return Response.ok(ReleaseStage.values()).build();
    }
}
