package org.kiwiproject.champagne.resource;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import org.apache.commons.lang3.StringUtils;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.ComponentDao;
import org.kiwiproject.champagne.dao.HostDao;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.model.Component;
import org.kiwiproject.champagne.model.Host;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;
import org.kiwiproject.jaxrs.exception.JaxrsNotFoundException;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.champagne.util.DeployableSystems.checkUserAdminOfSystem;
import static org.kiwiproject.champagne.util.DeployableSystems.getSystemIdOrThrowBadRequest;

@Path("/host")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class HostConfigurationResource extends AuditableResource {

    private final HostDao hostDao;
    private final ComponentDao componentDao;

    public HostConfigurationResource(HostDao hostDao, ComponentDao componentDao, AuditRecordDao auditRecordDao, ApplicationErrorDao errorDao) {
        super(auditRecordDao, errorDao);

        this.hostDao = hostDao;
        this.componentDao = componentDao;
    }
    
    @GET
    @Path("/{environment}")
    @Timed
    @ExceptionMetered
    public Response listHostsForEnvironment(@PathParam("environment") Long envId, @QueryParam("componentFilter") String componentFilter) {
        var systemId = getSystemIdOrThrowBadRequest();
        var hosts = isBlank(componentFilter) ? hostDao.findHostsByEnvId(envId, systemId) : List.of();

        return Response.ok(hosts).build();
    }

    @POST
    @Timed
    @ExceptionMetered
    public Response createHost(Host host) {
        checkUserAdminOfSystem();

        if (isNull(host.getDeployableSystemId())) {
            var systemId = getSystemIdOrThrowBadRequest();
            host = host.withDeployableSystemId(systemId);
        }

        var hostId = hostDao.insertHost(host, StringUtils.join(host.getTags(), ","));

        auditAction(hostId, Host.class, Action.CREATED);

        return Response.accepted().build();
    }

    @PUT
    @Path("/{id}")
    @Timed
    @ExceptionMetered
    public Response updateHost(Host host, @PathParam("id") Long hostId) {
        checkUserAdminOfSystem();

        var updateCount = hostDao.updateHost(host.getHostname(), StringUtils.join(host.getTags(), ","), hostId);

        if (updateCount > 0) {
            auditAction(hostId, Host.class, Action.UPDATED);
        }

        return Response.accepted().build();
    }

    @DELETE
    @Path("/{id}")
    @Timed
    @ExceptionMetered
    public Response deleteHost(@PathParam("id") Long id) {
        checkUserAdminOfSystem();

        var deleteCount = hostDao.deleteHost(id);

        if (deleteCount > 0) {
            auditAction(id, Host.class, Action.DELETED);
        }

        return Response.accepted().build();
    }

    @GET
    @Path("/components")
    @Timed
    @ExceptionMetered
    public Response listComponents() {
        var systemId = getSystemIdOrThrowBadRequest();
        var components = componentDao.findComponentsForSystem(systemId);

        return Response.ok(components).build();
    }

    @GET
    @Path("/{hostId}/components")
    @Timed
    @ExceptionMetered
    public Response listComponentsForHost(@PathParam("hostId") Long hostId) {
        var host = hostDao.findById(hostId).orElseThrow(() -> new JaxrsNotFoundException("No host found"));
        var components = componentDao.findComponentsByHostTags(host.getTags());

        return Response.ok(components).build();
    }

    @POST
    @Path("/component")
    @Timed
    @ExceptionMetered
    public Response createComponent(Component component) {
        checkUserAdminOfSystem();

        if (isNull(component.getDeployableSystemId())) {
            var systemId = getSystemIdOrThrowBadRequest();
            component = component.withDeployableSystemId(systemId);
        }

        var componentId = componentDao.insertComponent(component);

        auditAction(componentId, Component.class, Action.CREATED);

        return Response.accepted().build();
    }

    @PUT
    @Path("/component/{id}")
    @Timed
    @ExceptionMetered
    public Response updateComponent(Component component, @PathParam("id") Long componentId) {
        checkUserAdminOfSystem();

        var updateCount = componentDao.updateComponent(component.getComponentName(), component.getTag(), componentId);

        if (updateCount > 0) {
            auditAction(componentId, Component.class, Action.UPDATED);
        }

        return Response.accepted().build();
    }

    @DELETE
    @Path("/component/{componentId}")
    @Timed
    @ExceptionMetered
    public Response deleteComponent(@PathParam("componentId") Long componentId) {
        checkUserAdminOfSystem();

        var deleteCount = componentDao.deleteComponent(componentId);

        if (deleteCount > 0) {
            auditAction(componentId, Component.class, Action.DELETED);
        }

        return Response.accepted().build();
    }
}
