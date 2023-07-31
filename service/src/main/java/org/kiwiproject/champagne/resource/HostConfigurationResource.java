package org.kiwiproject.champagne.resource;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.champagne.util.DeployableSystems.checkUserAdminOfSystem;
import static org.kiwiproject.champagne.util.DeployableSystems.getSystemIdOrThrowBadRequest;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.ComponentDao;
import org.kiwiproject.champagne.dao.HostDao;
import org.kiwiproject.champagne.dao.TagDao;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.model.Component;
import org.kiwiproject.champagne.model.Host;
import org.kiwiproject.champagne.model.Tag;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;
import org.kiwiproject.jaxrs.exception.JaxrsNotFoundException;

@Path("/host")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class HostConfigurationResource extends AuditableResource {

    private final HostDao hostDao;
    private final ComponentDao componentDao;
    private final TagDao tagDao;

    public HostConfigurationResource(HostDao hostDao, ComponentDao componentDao, TagDao tagDao, AuditRecordDao auditRecordDao, ApplicationErrorDao errorDao) {
        super(auditRecordDao, errorDao);

        this.hostDao = hostDao;
        this.componentDao = componentDao;
        this.tagDao = tagDao;
    }
    
    @GET
    @Path("/{environment}")
    @Timed
    @ExceptionMetered
    public Response listHostsForEnvironment(@PathParam("environment") Long envId, @QueryParam("componentFilter") String componentFilter) {
        var systemId = getSystemIdOrThrowBadRequest();

        List<Host> hosts;

        if (isBlank(componentFilter)) {
            hosts = hostDao.findHostsByEnvId(envId, systemId);
            hosts = hosts.stream().map(host -> {
                var tags = tagDao.findTagsForHost(host.getId());
                return host.withTags(tags);
            }).toList();
        } else {
            var components = componentDao.findComponentsForSystemMatchingName(systemId, f("%{}%", componentFilter));
            var componentTags = components.stream().map(Component::getTagId).toList();

            if (componentTags.isEmpty()) {
                hosts = List.of();
            } else {
                hosts = hostDao.findHostsForTagsInEnv(systemId, envId, componentTags);
                hosts = hosts.stream().map(host -> {
                    var tags = tagDao.findTagsForHost(host.getId());
                    return host.withTags(tags);
                }).toList();
            }
        }

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

        var hostId = hostDao.insertHost(host);

        var tagIds = host.getTags().stream().map(Tag::getId).toList();

        if (!tagIds.isEmpty()) {
            hostDao.addTagsForHost(hostId, tagIds);
        }

        auditAction(hostId, Host.class, Action.CREATED);

        return Response.accepted().build();
    }

    @PUT
    @Path("/{id}")
    @Timed
    @ExceptionMetered
    public Response updateHost(Host host, @PathParam("id") Long hostId) {
        checkUserAdminOfSystem();

        var updateCount = hostDao.updateHost(host.getHostname(), hostId);

        var tagIds = host.getTags().stream().map(Tag::getId).toList();
        hostDao.updateTagList(hostId, tagIds);

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
        components = components.stream().map(component -> {
            if (nonNull(component.getTagId())) {
                return component.withTag(tagDao.findTagById(component.getTagId()));
            }
            return component;
        }).toList();

        return Response.ok(components).build();
    }

    @GET
    @Path("/{hostId}/components")
    @Timed
    @ExceptionMetered
    public Response listComponentsForHost(@PathParam("hostId") Long hostId) {
        var host = hostDao.findById(hostId).orElseThrow(() -> new JaxrsNotFoundException("No host found"));
        var tagIds = hostDao.findTagIdsForHost(host.getId());
        var components = componentDao.findComponentsByHostTags(tagIds);

        components = components.stream().map(component -> {
            if (nonNull(component.getTagId())) {
                return component.withTag(tagDao.findTagById(component.getTagId()));
            }
            return component;
        }).toList();

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

        var updateCount = componentDao.updateComponent(component.getComponentName(), component.getTagId(), componentId);

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
