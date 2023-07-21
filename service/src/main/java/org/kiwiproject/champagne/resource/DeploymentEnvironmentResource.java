package org.kiwiproject.champagne.resource;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.DeploymentEnvironmentDao;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.model.DeploymentEnvironment;
import org.kiwiproject.champagne.service.ManualTaskService;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static java.util.Objects.isNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;
import static org.kiwiproject.champagne.util.DeployableSystems.checkUserAdminOfSystem;
import static org.kiwiproject.champagne.util.DeployableSystems.getSystemIdOrThrowBadRequest;

@Path("/environments")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@PermitAll
public class DeploymentEnvironmentResource extends AuditableResource {
    
    private final DeploymentEnvironmentDao deploymentEnvironmentDao;
    private final ManualTaskService manualTaskService;

    public DeploymentEnvironmentResource(DeploymentEnvironmentDao deploymentEnvironmentDao, AuditRecordDao auditRecordDao, ApplicationErrorDao errorDao, ManualTaskService manualTaskService) {
        super(auditRecordDao, errorDao);

        this.deploymentEnvironmentDao = deploymentEnvironmentDao;
        this.manualTaskService = manualTaskService;
    }

    @GET
    @Timed
    @ExceptionMetered
    public Response listEnvironments() {
        var systemId = getSystemIdOrThrowBadRequest();
        var envs = deploymentEnvironmentDao.findAllEnvironments(systemId);

        return Response.ok(envs).build();
    }

    @POST
    @Timed
    @ExceptionMetered
    public Response createEnvironment(@Valid DeploymentEnvironment deploymentEnvironment) {
        checkUserAdminOfSystem();

        if (isNull(deploymentEnvironment.getDeployableSystemId())) {
            var systemId = getSystemIdOrThrowBadRequest();
            deploymentEnvironment = deploymentEnvironment.withDeployableSystemId(systemId);
        }

        var id = deploymentEnvironmentDao.insertEnvironment(deploymentEnvironment);

        auditAction(id, DeploymentEnvironment.class, Action.CREATED);

        manualTaskService.addManualReleaseAndTaskStatusForNewEnv(id);

        return Response.noContent().build();
    }

    @PUT
    @Timed
    @ExceptionMetered
    public Response updateEnvironment(@Valid DeploymentEnvironment deploymentEnvironment) {
        checkUserAdminOfSystem();

        requireNotNull(deploymentEnvironment.getId(), "An id is required to update a deployment environment");

        var updatedCount = deploymentEnvironmentDao.updateEnvironment(deploymentEnvironment);

        if (updatedCount > 0) {
            auditAction(deploymentEnvironment.getId(), DeploymentEnvironment.class, Action.UPDATED);
        }

        return Response.accepted().build();
    }
    
    @DELETE
    @Path("/{id}/delete")
    @Timed
    @ExceptionMetered
    public Response hardDeleteEnvironment(@PathParam("id") Long id) {
        checkUserAdminOfSystem();

        var deleteCount = deploymentEnvironmentDao.hardDeleteById(id);

        if (deleteCount > 0) {
            auditAction(id, DeploymentEnvironment.class, Action.DELETED);
        }

        return Response.accepted().build();
    }

    @DELETE
    @Path("/{id}/deactivate")
    @Timed
    @ExceptionMetered
    public Response deactivateEnvironment(@PathParam("id") Long id) {
        checkUserAdminOfSystem();

        var updatedCount = deploymentEnvironmentDao.softDeleteById(id);

        if (updatedCount > 0) {
            auditAction(id, DeploymentEnvironment.class, Action.UPDATED);
        }

        return Response.accepted().build();
    }

    @PUT
    @Path("/{id}/activate")
    @Timed
    @ExceptionMetered
    public Response activateEnvironment(@PathParam("id") Long id) {
        checkUserAdminOfSystem();

        var updatedCount = deploymentEnvironmentDao.unSoftDeleteById(id);

        if (updatedCount > 0) {
            auditAction(id, DeploymentEnvironment.class, Action.UPDATED);
        }

        return Response.accepted().build();
    }
}
