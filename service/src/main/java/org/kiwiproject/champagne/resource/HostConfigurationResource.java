package org.kiwiproject.champagne.resource;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.ComponentDao;
import org.kiwiproject.champagne.dao.HostDao;
import org.kiwiproject.champagne.model.Component;
import org.kiwiproject.champagne.model.Host;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.jaxrs.exception.JaxrsNotFoundException;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

@Path("/host")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class HostConfigurationResource extends AuditableResource {

    private final HostDao hostDao;
    private final ComponentDao componentDao;

    public HostConfigurationResource(HostDao hostDao, ComponentDao componentDao, AuditRecordDao auditRecordDao) {
        super(auditRecordDao);

        this.hostDao = hostDao;
        this.componentDao = componentDao;
    }
    
    @GET
    @Path("/{environment}")
    @Timed
    @ExceptionMetered
    public Response listHostsForEnvironment(@PathParam("environment") Long envId, @QueryParam("componentFilter") String componentFilter) {
        var hosts = isBlank(componentFilter) ? hostDao.findHostsByEnvId(envId) : List.of();

        return Response.ok(hosts).build();
    }

    @POST
    @Timed
    @ExceptionMetered
    public Response createHost(Host host) {
        var hostId = hostDao.insertHost(host, StringUtils.join(host.getTags(), ","));

        auditAction(hostId, Host.class, Action.CREATED);

        return Response.accepted().build();
    }

    @DELETE
    @Path("/{id}")
    @Timed
    @ExceptionMetered
    public Response deleteHost(@PathParam("id") Long id) {
        var deleteCount = hostDao.deleteHost(id);

        if (deleteCount > 0) {
            auditAction(id, Host.class, Action.DELETED);
        }

        return Response.accepted().build();
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
        var componentId = componentDao.insertComponent(component);

        auditAction(componentId, Component.class, Action.CREATED);

        return Response.accepted().build();
    }

    @DELETE
    @Path("/component/{componentId}")
    @Timed
    @ExceptionMetered
    public Response deleteComponent(@PathParam("componentId") Long componentId) {
        var deleteCount = componentDao.deleteComponent(componentId);

        if (deleteCount > 0) {
            auditAction(componentId, Component.class, Action.DELETED);
        }

        return Response.accepted().build();
    }
}
