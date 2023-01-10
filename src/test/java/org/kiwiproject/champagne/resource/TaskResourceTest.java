package org.kiwiproject.champagne.resource;

import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.constants.KiwiTestConstants.JSON_HELPER;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertAcceptedResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertNotFoundResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.GenericType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.champagne.core.DeploymentEnvironment;
import org.kiwiproject.champagne.core.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.core.manualdeployment.Release;
import org.kiwiproject.champagne.core.manualdeployment.ReleaseStage;
import org.kiwiproject.champagne.core.manualdeployment.ReleaseStatus;
import org.kiwiproject.champagne.core.manualdeployment.Task;
import org.kiwiproject.champagne.core.manualdeployment.TaskStatus;
import org.kiwiproject.champagne.jdbi.DeploymentEnvironmentDao;
import org.kiwiproject.champagne.jdbi.ReleaseDao;
import org.kiwiproject.champagne.jdbi.ReleaseStatusDao;
import org.kiwiproject.champagne.jdbi.TaskDao;
import org.kiwiproject.champagne.jdbi.TaskStatusDao;
import org.kiwiproject.dropwizard.util.exception.JerseyViolationExceptionMapper;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;
import org.kiwiproject.json.JsonHelper;
import org.kiwiproject.spring.data.KiwiPage;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

@DisplayName("TaskResource")
@ExtendWith(DropwizardExtensionsSupport.class)
class TaskResourceTest {

    private static final ReleaseDao RELEASE_DAO = mock(ReleaseDao.class);
    private static final ReleaseStatusDao RELEASE_STATUS_DAO = mock(ReleaseStatusDao.class);
    private static final TaskDao TASK_DAO = mock(TaskDao.class);
    private static final TaskStatusDao TASK_STATUS_DAO = mock(TaskStatusDao.class);
    private static final DeploymentEnvironmentDao DEPLOYMENT_ENVIRONMENT_DAO = mock(DeploymentEnvironmentDao.class);

    private static final TaskResource RESOURCE = new TaskResource(RELEASE_DAO, RELEASE_STATUS_DAO, TASK_DAO, TASK_STATUS_DAO, DEPLOYMENT_ENVIRONMENT_DAO);

    private static final ResourceExtension RESOURCES = ResourceExtension.builder()
        .bootstrapLogging(false)
        .addResource(RESOURCE)
        .addProvider(JerseyViolationExceptionMapper.class)
        .addProvider(JaxrsExceptionMapper.class)
        .build();

    @AfterEach
    void cleanup() {
        reset(RELEASE_DAO, RELEASE_STATUS_DAO, TASK_DAO, TASK_STATUS_DAO, DEPLOYMENT_ENVIRONMENT_DAO);
    }

    @Nested
    class GetPagedReleases {

        @Test
        void shouldReturnPagedListOfReleases() {
            var release = Release.builder()
                .id(1L)
                .releaseNumber("2023.42")
                .build();

            when(RELEASE_DAO.findPagedReleases(0, 10)).thenReturn(List.of(release));
            when(RELEASE_DAO.countReleases()).thenReturn(1L);

            var releaseStatus = ReleaseStatus.builder()
                .releaseId(1L)
                .status(DeploymentTaskStatus.PENDING)
                .environmentId(1L)
                .build();

            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(releaseStatus));

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/releases")
                .queryParam("pageNumber", 1)
                .queryParam("pageSize", 10)
                .request()
                .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<KiwiPage<ReleaseWithStatus>>(){});

            assertThat(result.getNumber()).isOne();
            assertThat(result.getTotalElements()).isOne();

            var releaseWithStatus = first(result.getContent());
            assertThat(releaseWithStatus.getId()).isOne();
            assertThat(releaseWithStatus.getReleaseNumber()).isEqualTo("2023.42");
            assertThat(releaseWithStatus.getEnvironmentStatus()).contains(entry(1L, DeploymentTaskStatus.PENDING));

            verify(RELEASE_DAO).findPagedReleases(0, 10);
            verify(RELEASE_DAO).countReleases();
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);

            verifyNoMoreInteractions(RELEASE_DAO, RELEASE_STATUS_DAO);
            verifyNoInteractions(TASK_DAO, TASK_STATUS_DAO, DEPLOYMENT_ENVIRONMENT_DAO);
        }

        @Test
        void shouldReturnPagedListOfReleasesWithDefaultPaging() {
            var release = Release.builder()
                .id(1L)
                .releaseNumber("2023.42")
                .build();

            when(RELEASE_DAO.findPagedReleases(0, 50)).thenReturn(List.of(release));
            when(RELEASE_DAO.countReleases()).thenReturn(1L);

            var releaseStatus = ReleaseStatus.builder()
                .releaseId(1L)
                .status(DeploymentTaskStatus.PENDING)
                .environmentId(1L)
                .build();

            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(releaseStatus));

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/releases")
                .request()
                .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<KiwiPage<ReleaseWithStatus>>(){});

            assertThat(result.getNumber()).isOne();
            assertThat(result.getSize()).isEqualTo(50);
            assertThat(result.getTotalElements()).isOne();

            var releaseWithStatus = first(result.getContent());
            assertThat(releaseWithStatus.getId()).isOne();
            assertThat(releaseWithStatus.getReleaseNumber()).isEqualTo("2023.42");
            assertThat(releaseWithStatus.getEnvironmentStatus()).contains(entry(1L, DeploymentTaskStatus.PENDING));

            verify(RELEASE_DAO).findPagedReleases(0, 50);
            verify(RELEASE_DAO).countReleases();
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);

            verifyNoMoreInteractions(RELEASE_DAO, RELEASE_STATUS_DAO);
            verifyNoInteractions(TASK_DAO, TASK_STATUS_DAO, DEPLOYMENT_ENVIRONMENT_DAO);
        }
    }
    
    @Nested
    class GetTasksForRelease {

        @Test
        void shouldReturnTasks() {
            var task = Task.builder()
                .id(1L)
                .releaseId(1L)
                .component("ZK")
                .summary("Upgrade")
                .description("All the steps")
                .stage(ReleaseStage.PRE)
                .build();

            when(TASK_DAO.findByReleaseId(1L)).thenReturn(List.of(task));

            var taskStatus = TaskStatus.builder()
                .taskId(1L)
                .status(DeploymentTaskStatus.PENDING)
                .environmentId(1L)
                .build();

            when(TASK_STATUS_DAO.findByTaskId(1L)).thenReturn(List.of(taskStatus));

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/releases/{releaseId}")
                .resolveTemplate("releaseId", 1L)
                .request()
                .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<List<TaskWithStatus>>() {});

            var taskWithStatus = first(result);

            assertThat(taskWithStatus.getId()).isOne();
            assertThat(taskWithStatus.getComponent()).isEqualTo(task.getComponent());
            assertThat(taskWithStatus.getSummary()).isEqualTo(task.getSummary());
            assertThat(taskWithStatus.getDescription()).isEqualTo(task.getDescription());
            assertThat(taskWithStatus.getStage()).isEqualTo(task.getStage());
            assertThat(taskWithStatus.getEnvironmentStatus()).contains(entry(1L, DeploymentTaskStatus.PENDING));

            verify(TASK_DAO).findByReleaseId(1L);
            verify(TASK_STATUS_DAO).findByTaskId(1L);

            verifyNoMoreInteractions(TASK_DAO, TASK_STATUS_DAO);
            verifyNoInteractions(RELEASE_DAO, RELEASE_STATUS_DAO, DEPLOYMENT_ENVIRONMENT_DAO);
        }
    }

    @Nested
    class AddNewRelease {

        @Test
        void shouldSaveNewRelease() {
            var release = Release.builder()
                .releaseNumber("2023.01")
                .build();

            when(RELEASE_DAO.insertRelease(any(Release.class))).thenReturn(1L);

            var env = DeploymentEnvironment.builder()
                .id(1L)
                .name("DEVELOPMENT")
                .build();

            when(DEPLOYMENT_ENVIRONMENT_DAO.findAllEnvironments()).thenReturn(List.of(env));

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/releases")
                .request()
                .post(json(release));

            assertAcceptedResponse(response);

            verify(RELEASE_DAO).insertRelease(argThat(r -> "2023.01".equalsIgnoreCase(r.getReleaseNumber())));
            verify(RELEASE_STATUS_DAO).insertReleaseStatus(any(ReleaseStatus.class));
            verify(DEPLOYMENT_ENVIRONMENT_DAO).findAllEnvironments();

            verifyNoMoreInteractions(RELEASE_DAO, RELEASE_STATUS_DAO, DEPLOYMENT_ENVIRONMENT_DAO);
            verifyNoInteractions(TASK_DAO, TASK_STATUS_DAO);
        }
    }
    
    @Nested
    class AddNewTask {

        @Test
        void shouldSaveNewTask() {
            var task = Task.builder()
                .releaseId(1L)
                .stage(ReleaseStage.PRE)
                .description("some things")
                .summary("Do it")
                .component("super-service")
                .build();

            when(TASK_DAO.insertTask(any(Task.class))).thenReturn(2L);

            var env = DeploymentEnvironment.builder()
                .id(1L)
                .name("DEVELOPMENT")
                .build();

            when(DEPLOYMENT_ENVIRONMENT_DAO.findAllEnvironments()).thenReturn(List.of(env));

            var taskStatus = TaskStatus.builder()
                .status(DeploymentTaskStatus.COMPLETE)
                .environmentId(1L)
                .build();

            when(TASK_STATUS_DAO.findByTaskId(2L)).thenReturn(List.of(taskStatus));

            var savedTask = Task.builder()
                .id(2L)
                .releaseId(1L)
                .stage(ReleaseStage.PRE)
                .description("some things")
                .summary("Do it")
                .component("super-service")
                .build();

            when(TASK_DAO.findByReleaseId(1L)).thenReturn(List.of(savedTask));

            var status = ReleaseStatus.builder()
                .id(5L)
                .environmentId(1L)
                .status(DeploymentTaskStatus.PENDING)
                .build();

            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(status));

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks")
                .request()
                .post(json(task));

            assertAcceptedResponse(response);

            verify(TASK_DAO).insertTask(argThat(t -> JSON_HELPER.jsonEqualsIgnoringPaths(task, t, "id", "createdAt", "updatedAt")));
            verify(TASK_STATUS_DAO).insertTaskStatus(any(TaskStatus.class));
            verify(TASK_DAO).findByReleaseId(1L);
            verify(TASK_STATUS_DAO).findByTaskId(2L);
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);
            verify(RELEASE_STATUS_DAO).updateStatus(5L, DeploymentTaskStatus.COMPLETE);
            verify(DEPLOYMENT_ENVIRONMENT_DAO).findAllEnvironments();

            verifyNoMoreInteractions(TASK_DAO, TASK_STATUS_DAO, RELEASE_STATUS_DAO, DEPLOYMENT_ENVIRONMENT_DAO);
            verifyNoInteractions(RELEASE_DAO);

        }
    }

    @Nested
    class UpdateReleaseStatus {

        @Test
        void shouldUpdateStatusForRelease() {
            when(RELEASE_STATUS_DAO.updateStatus(1L, DeploymentTaskStatus.COMPLETE)).thenReturn(1);

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/releases/{statusId}/{status}")
                .resolveTemplate("statusId", 1L)
                .resolveTemplate("status", DeploymentTaskStatus.COMPLETE)
                .request()
                .put(json(""));

            assertAcceptedResponse(response);

            verify(RELEASE_STATUS_DAO).updateStatus(1L, DeploymentTaskStatus.COMPLETE);
        }

        @Test
        void shouldReturn404ResponseWhenStatusNotFound() {
            when(RELEASE_STATUS_DAO.updateStatus(1L, DeploymentTaskStatus.COMPLETE)).thenReturn(0);

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/releases/{statusId}/{status}")
                .resolveTemplate("statusId", 1L)
                .resolveTemplate("status", DeploymentTaskStatus.COMPLETE)
                .request()
                .put(json(""));

            assertNotFoundResponse(response);
        }
    }

    @Nested
    class UpdateTaskStatus {

        @Test
        void shouldUpdateStatusForTask() {
            when(TASK_STATUS_DAO.updateStatus(1L, DeploymentTaskStatus.COMPLETE)).thenReturn(1);

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/{statusId}/{status}")
                .resolveTemplate("statusId", 1L)
                .resolveTemplate("status", DeploymentTaskStatus.COMPLETE)
                .request()
                .put(json(""));

            assertAcceptedResponse(response);

            verify(TASK_STATUS_DAO).updateStatus(1L, DeploymentTaskStatus.COMPLETE);
        }

        @Test
        void shouldReturn404ResponseWhenStatusNotFound() {
            when(TASK_STATUS_DAO.updateStatus(1L, DeploymentTaskStatus.COMPLETE)).thenReturn(0);

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/{statusId}/{status}")
                .resolveTemplate("statusId", 1L)
                .resolveTemplate("status", DeploymentTaskStatus.COMPLETE)
                .request()
                .put(json(""));

            assertNotFoundResponse(response);
        }
    }

    @Nested
    class DeleteRelease {

        @Test
        void shouldDeleteRelease() {
            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/releases/{releaseId}")
                .resolveTemplate("releaseId", 1L)
                .request()
                .delete();

            assertAcceptedResponse(response);
            verify(RELEASE_DAO).deleteById(1L);
        }
    }

    @Nested
    class DeleteTask {

        @Test
        void shouldDeleteTask() {
            var task = Task.builder()
                .id(1L)
                .releaseId(1L)
                .build();
            
            when(TASK_DAO.findById(1L)).thenReturn(Optional.of(task));

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/{taskId}")
                .resolveTemplate("taskId", 1L)
                .request()
                .delete();

            assertAcceptedResponse(response);
            verify(TASK_DAO).deleteById(1L);
        }

        @Test
        void shouldReturn404WhenTaskNotFound() {
            when(TASK_DAO.findById(1L)).thenReturn(Optional.empty());

            var response = RESOURCES.client()
                .target("/manual/deployment/tasks/{taskId}")
                .resolveTemplate("taskId", 1L)
                .request()
                .delete();

            assertNotFoundResponse(response);
        }
    }

    @Nested
    class CalculateReleaseStatus {

        @Test
        void shouldKeepOriginalStatusIfNoTasks() {
            when(TASK_DAO.findByReleaseId(1L)).thenReturn(List.of());

            var releaseStatus = newReleaseStatus(DeploymentTaskStatus.PENDING);
            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(releaseStatus));

            RESOURCE.calculateReleaseStatus(1L);

            verify(TASK_DAO).findByReleaseId(1L);
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);

            verifyNoMoreInteractions(TASK_DAO, RELEASE_STATUS_DAO);
            verifyNoInteractions(TASK_STATUS_DAO, RELEASE_DAO);
        }

        private ReleaseStatus newReleaseStatus(DeploymentTaskStatus status) {
            return ReleaseStatus.builder()
                .id(1L)
                .environmentId(1L)
                .status(status)
                .build();
        }

        @Test
        void shouldIgnoreStatusUpdateWhenStatusDoesNotChange() {
            var task = Task.builder().id(2L).build();
            when(TASK_DAO.findByReleaseId(1L)).thenReturn(List.of(task));

            var taskStatus = newTaskStatus(DeploymentTaskStatus.PENDING);
            when(TASK_STATUS_DAO.findByTaskId(2L)).thenReturn(List.of(taskStatus));

            var releaseStatus = newReleaseStatus(DeploymentTaskStatus.PENDING);
            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(releaseStatus));

            RESOURCE.calculateReleaseStatus(1L);

            verify(TASK_DAO).findByReleaseId(1L);
            verify(TASK_STATUS_DAO).findByTaskId(2L);
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);

            verifyNoMoreInteractions(TASK_DAO, RELEASE_STATUS_DAO, TASK_STATUS_DAO);
            verifyNoInteractions(RELEASE_DAO);
        }

        private TaskStatus newTaskStatus(DeploymentTaskStatus status) {
            return TaskStatus.builder()
                .id(1L)
                .environmentId(1L)
                .status(status)
                .build();
        }

        @Test
        void shouldSetStatusToCompleteWhenOnlyTaskIsComplete() {
            var task = Task.builder().id(2L).build();
            when(TASK_DAO.findByReleaseId(1L)).thenReturn(List.of(task));

            var taskStatus = newTaskStatus(DeploymentTaskStatus.COMPLETE);
            when(TASK_STATUS_DAO.findByTaskId(2L)).thenReturn(List.of(taskStatus));

            var releaseStatus = newReleaseStatus(DeploymentTaskStatus.PENDING);
            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(releaseStatus));

            RESOURCE.calculateReleaseStatus(1L);

            verify(TASK_DAO).findByReleaseId(1L);
            verify(TASK_STATUS_DAO).findByTaskId(2L);
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);
            verify(RELEASE_STATUS_DAO).updateStatus(1L, DeploymentTaskStatus.COMPLETE);

            verifyNoMoreInteractions(TASK_DAO, RELEASE_STATUS_DAO, TASK_STATUS_DAO);
            verifyNoInteractions(RELEASE_DAO);
        }

        @Test
        void shouldSetStatusToCompleteWhenAllTasksAreComplete() {
            var task = Task.builder().id(2L).build();
            var task2 = Task.builder().id(3L).build();
            when(TASK_DAO.findByReleaseId(1L)).thenReturn(List.of(task, task2));

            var taskStatus = newTaskStatus(DeploymentTaskStatus.COMPLETE);
            when(TASK_STATUS_DAO.findByTaskId(2L)).thenReturn(List.of(taskStatus));
            when(TASK_STATUS_DAO.findByTaskId(3L)).thenReturn(List.of(taskStatus));

            var releaseStatus = newReleaseStatus(DeploymentTaskStatus.PENDING);
            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(releaseStatus));

            RESOURCE.calculateReleaseStatus(1L);

            verify(TASK_DAO).findByReleaseId(1L);
            verify(TASK_STATUS_DAO).findByTaskId(2L);
            verify(TASK_STATUS_DAO).findByTaskId(3L);
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);
            verify(RELEASE_STATUS_DAO).updateStatus(1L, DeploymentTaskStatus.COMPLETE);

            verifyNoMoreInteractions(TASK_DAO, RELEASE_STATUS_DAO, TASK_STATUS_DAO);
            verifyNoInteractions(RELEASE_DAO);
        }

        @Test
        void shouldSetStatusToNotRequiredWhenOnlyTaskIsNotRequired() {
            var task = Task.builder().id(2L).build();
            when(TASK_DAO.findByReleaseId(1L)).thenReturn(List.of(task));

            var taskStatus = newTaskStatus(DeploymentTaskStatus.NOT_REQUIRED);
            when(TASK_STATUS_DAO.findByTaskId(2L)).thenReturn(List.of(taskStatus));

            var releaseStatus = newReleaseStatus(DeploymentTaskStatus.PENDING);
            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(releaseStatus));

            RESOURCE.calculateReleaseStatus(1L);

            verify(TASK_DAO).findByReleaseId(1L);
            verify(TASK_STATUS_DAO).findByTaskId(2L);
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);
            verify(RELEASE_STATUS_DAO).updateStatus(1L, DeploymentTaskStatus.NOT_REQUIRED);

            verifyNoMoreInteractions(TASK_DAO, RELEASE_STATUS_DAO, TASK_STATUS_DAO);
            verifyNoInteractions(RELEASE_DAO);
        }

        @Test
        void shouldSetStatusToNotRequiredWhenAllTasksAreNotRequired() {
            var task = Task.builder().id(2L).build();
            var task2 = Task.builder().id(3L).build();
            when(TASK_DAO.findByReleaseId(1L)).thenReturn(List.of(task, task2));

            var taskStatus = newTaskStatus(DeploymentTaskStatus.NOT_REQUIRED);
            when(TASK_STATUS_DAO.findByTaskId(2L)).thenReturn(List.of(taskStatus));
            when(TASK_STATUS_DAO.findByTaskId(3L)).thenReturn(List.of(taskStatus));

            var releaseStatus = newReleaseStatus(DeploymentTaskStatus.PENDING);
            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(releaseStatus));

            RESOURCE.calculateReleaseStatus(1L);

            verify(TASK_DAO).findByReleaseId(1L);
            verify(TASK_STATUS_DAO).findByTaskId(2L);
            verify(TASK_STATUS_DAO).findByTaskId(3L);
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);
            verify(RELEASE_STATUS_DAO).updateStatus(1L, DeploymentTaskStatus.NOT_REQUIRED);

            verifyNoMoreInteractions(TASK_DAO, RELEASE_STATUS_DAO, TASK_STATUS_DAO);
            verifyNoInteractions(RELEASE_DAO);
        }

        @Test
        void shouldSetStatusToPendingWhenAllTasksArePending() {
            var task = Task.builder().id(2L).build();
            var task2 = Task.builder().id(3L).build();
            when(TASK_DAO.findByReleaseId(1L)).thenReturn(List.of(task, task2));

            var taskStatus = newTaskStatus(DeploymentTaskStatus.PENDING);
            when(TASK_STATUS_DAO.findByTaskId(2L)).thenReturn(List.of(taskStatus));
            when(TASK_STATUS_DAO.findByTaskId(3L)).thenReturn(List.of(taskStatus));

            var releaseStatus = newReleaseStatus(DeploymentTaskStatus.COMPLETE);
            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(releaseStatus));

            RESOURCE.calculateReleaseStatus(1L);

            verify(TASK_DAO).findByReleaseId(1L);
            verify(TASK_STATUS_DAO).findByTaskId(2L);
            verify(TASK_STATUS_DAO).findByTaskId(3L);
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);
            verify(RELEASE_STATUS_DAO).updateStatus(1L, DeploymentTaskStatus.PENDING);

            verifyNoMoreInteractions(TASK_DAO, RELEASE_STATUS_DAO, TASK_STATUS_DAO);
            verifyNoInteractions(RELEASE_DAO);
        }

        @Test
        void shouldSetStatusToPendingWhenSomeTasksArePending() {
            var task = Task.builder().id(2L).build();
            var task2 = Task.builder().id(3L).build();
            when(TASK_DAO.findByReleaseId(1L)).thenReturn(List.of(task, task2));

            var taskStatus = newTaskStatus(DeploymentTaskStatus.PENDING);
            when(TASK_STATUS_DAO.findByTaskId(2L)).thenReturn(List.of(taskStatus));

            var taskStatus2 = newTaskStatus(DeploymentTaskStatus.COMPLETE);
            when(TASK_STATUS_DAO.findByTaskId(3L)).thenReturn(List.of(taskStatus2));

            var releaseStatus = newReleaseStatus(DeploymentTaskStatus.COMPLETE);
            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(releaseStatus));

            RESOURCE.calculateReleaseStatus(1L);

            verify(TASK_DAO).findByReleaseId(1L);
            verify(TASK_STATUS_DAO).findByTaskId(2L);
            verify(TASK_STATUS_DAO).findByTaskId(3L);
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);
            verify(RELEASE_STATUS_DAO).updateStatus(1L, DeploymentTaskStatus.PENDING);

            verifyNoMoreInteractions(TASK_DAO, RELEASE_STATUS_DAO, TASK_STATUS_DAO);
            verifyNoInteractions(RELEASE_DAO);
        }

        @Test
        void shouldSetStatusToCompleteWhenTasksContainCompleteAndNotRequired() {
            var task = Task.builder().id(2L).build();
            var task2 = Task.builder().id(3L).build();
            when(TASK_DAO.findByReleaseId(1L)).thenReturn(List.of(task, task2));

            var taskStatus = newTaskStatus(DeploymentTaskStatus.NOT_REQUIRED);
            when(TASK_STATUS_DAO.findByTaskId(2L)).thenReturn(List.of(taskStatus));

            var taskStatus2 = newTaskStatus(DeploymentTaskStatus.COMPLETE);
            when(TASK_STATUS_DAO.findByTaskId(3L)).thenReturn(List.of(taskStatus2));

            var releaseStatus = newReleaseStatus(DeploymentTaskStatus.PENDING);
            when(RELEASE_STATUS_DAO.findByReleaseId(1L)).thenReturn(List.of(releaseStatus));

            RESOURCE.calculateReleaseStatus(1L);

            verify(TASK_DAO).findByReleaseId(1L);
            verify(TASK_STATUS_DAO).findByTaskId(2L);
            verify(TASK_STATUS_DAO).findByTaskId(3L);
            verify(RELEASE_STATUS_DAO).findByReleaseId(1L);
            verify(RELEASE_STATUS_DAO).updateStatus(1L, DeploymentTaskStatus.COMPLETE);

            verifyNoMoreInteractions(TASK_DAO, RELEASE_STATUS_DAO, TASK_STATUS_DAO);
            verifyNoInteractions(RELEASE_DAO);
        }
    }
}
