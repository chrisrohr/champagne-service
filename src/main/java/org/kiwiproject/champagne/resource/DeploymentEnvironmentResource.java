package org.kiwiproject.champagne.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.DeploymentEnvironmentDao;
import org.kiwiproject.champagne.model.DeploymentEnvironment;
import org.kiwiproject.champagne.model.AuditRecord.Action;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

@Path("/environments")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@PermitAll
public class DeploymentEnvironmentResource extends AuditableResource {
    
    private final DeploymentEnvironmentDao deploymentEnvironmentDao;

    public DeploymentEnvironmentResource(DeploymentEnvironmentDao deploymentEnvironmentDao, AuditRecordDao auditRecordDao) {
        super(auditRecordDao);

        this.deploymentEnvironmentDao = deploymentEnvironmentDao;
    }

    @GET
    @Timed
    @ExceptionMetered
    public Response listEnvironments() {
        var envs = deploymentEnvironmentDao.findAllEnvironments();

        return Response.ok(envs).build();
    }

    @POST
    @Timed
    @ExceptionMetered
    public Response createEnvironment(@Valid DeploymentEnvironment deploymentEnvironment) {
        var id = deploymentEnvironmentDao.insertEnvironment(deploymentEnvironment);

        auditAction(id, DeploymentEnvironment.class, Action.CREATED);

        return Response.noContent().build();
    }

    @PUT
    @Timed
    @ExceptionMetered
    public Response updateEnvironment(@Valid DeploymentEnvironment deploymentEnvironment) {
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
        var updatedCount = deploymentEnvironmentDao.unSoftDeleteById(id);

        if (updatedCount > 0) {
            auditAction(id, DeploymentEnvironment.class, Action.UPDATED);
        }

        return Response.accepted().build();
    }
}
