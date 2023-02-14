package org.kiwiproject.champagne.resource;

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
import org.kiwiproject.champagne.dao.HostDao;
import org.kiwiproject.champagne.model.Host;
import org.kiwiproject.champagne.model.AuditRecord.Action;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

@Path("/host")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class HostConfigurationResource extends AuditableResource {

    private final HostDao hostDao;

    public HostConfigurationResource(HostDao hostDao, AuditRecordDao auditRecordDao) {
        super(auditRecordDao);

        this.hostDao = hostDao;
    }
    
    @GET
    @Path("/{environment}")
    @Timed
    @ExceptionMetered
    public Response listHostsForEnvironment(@PathParam("environment") Long envId, @QueryParam("componentFilter") String componentFilter) {
        List<Host> hosts;

        if (StringUtils.isBlank(componentFilter)) {
            hosts = hostDao.findHostsByEnvId(envId);
        } else {
            hosts = List.of();
        }

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
}
