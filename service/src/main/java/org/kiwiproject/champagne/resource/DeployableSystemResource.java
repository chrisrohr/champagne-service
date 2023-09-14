package org.kiwiproject.champagne.resource;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.champagne.model.AuditRecord.Action.CREATED;
import static org.kiwiproject.champagne.model.AuditRecord.Action.DELETED;
import static org.kiwiproject.champagne.model.AuditRecord.Action.UPDATED;
import static org.kiwiproject.search.KiwiSearching.zeroBasedOffset;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.dhatim.dropwizard.jwt.cookie.authentication.DefaultJwtCookiePrincipal;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.DeployableSystemDao;
import org.kiwiproject.champagne.dao.DeploymentEnvironmentDao;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.champagne.model.DeployableSystem;
import org.kiwiproject.champagne.model.DeployableSystem.SystemUser;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;
import org.kiwiproject.jaxrs.exception.JaxrsNotAuthorizedException;
import org.kiwiproject.spring.data.KiwiPage;

@Path("/systems")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@PermitAll
public class DeployableSystemResource extends AuditableResource {

    private final DeployableSystemDao deployableSystemDao;
    private final UserDao userDao;
    private final DeploymentEnvironmentDao deploymentEnvironmentDao;

    public DeployableSystemResource(DeployableSystemDao deployableSystemDao,
                                    UserDao userDao, DeploymentEnvironmentDao deploymentEnvironmentDao,
                                    AuditRecordDao auditRecordDao,
                                    ApplicationErrorDao errorDao) {

        super(auditRecordDao, errorDao);

        this.deployableSystemDao = deployableSystemDao;
        this.userDao = userDao;
        this.deploymentEnvironmentDao = deploymentEnvironmentDao;
    }

    @GET
    @Timed
    @ExceptionMetered
    public Response listSystemsForCurrentUser(@Auth DefaultJwtCookiePrincipal principal) {
        var loggedInUser = userDao.findBySystemIdentifier(principal.getName())
                .orElseThrow(() -> new JaxrsNotAuthorizedException("Must be logged in to list systems"));

        var systems = deployableSystemDao.findDeployableSystemsForUser(loggedInUser.getId());

        return Response.ok(systems).build();
    }

    @GET
    @Path("/admin")
    @Timed
    @ExceptionMetered
    @RolesAllowed({ "admin" })
    public Response listAllSystems(@QueryParam("pageNumber") @DefaultValue("1") int pageNumber,
                                   @QueryParam("pageSize") @DefaultValue("25") int pageSize) {

        var offset = zeroBasedOffset(pageNumber, pageSize);

        var systems = deployableSystemDao.findPagedDeployableSystems(offset, pageSize).stream()
                .map(this::inflateSystem)
                .toList();

        var total = deployableSystemDao.countDeployableSystems();

        var page = KiwiPage.of(pageNumber, pageSize, total, systems);
        return Response.ok(page).build();
    }

    private DeployableSystem inflateSystem(DeployableSystem system) {
        var updatedSystem = system.withUsers(deployableSystemDao.findUsersForSystem(system.getId()));

        if (Objects.nonNull(system.getDevEnvironmentId())) {
            updatedSystem = updatedSystem.withDevEnvName(deploymentEnvironmentDao.getEnvironmentName(system.getDevEnvironmentId()));
        }

        if (isNotBlank(system.getEnvironmentPromotionOrder())) {
            var orderedEnvNames = Arrays.stream(system.getEnvironmentPromotionOrder().split(","))
                    .map(Long::parseLong)
                    .map(deploymentEnvironmentDao::getEnvironmentName)
                    .toList();

            updatedSystem = updatedSystem.withEnvOrder(orderedEnvNames);
        }

        return updatedSystem;
    }

    @POST
    @Timed
    @ExceptionMetered
    @RolesAllowed({ "admin" })
    public Response createSystem(@Valid DeployableSystem system) {
        var id = deployableSystemDao.insertDeployableSystem(system);

        auditAction(id, DeployableSystem.class, CREATED);

        return Response.accepted().build();
    }

    @PUT
    @Path("/{id}/dev/{envId}")
    @Timed
    @ExceptionMetered
    public Response setDevEnvironmentOnSystem(@PathParam("id") Long id, @PathParam("envId") Long envId, @Auth DefaultJwtCookiePrincipal user) {
        var loggedInUser = userDao.findBySystemIdentifier(user.getName())
                .orElseThrow(() -> new JaxrsNotAuthorizedException("Must be logged in to update systems"));

        var isAdmin = deployableSystemDao.isUserAdminOfSystem(loggedInUser.getId(), id);

        if (!isAdmin) {
            throw new JaxrsNotAuthorizedException("User is not an admin of this system");
        }

        var count = deployableSystemDao.updateDevEnvironment(id, envId);

        if (count > 0) {
            auditAction(id, DeployableSystem.class, UPDATED);
        }

        return Response.accepted().build();
    }

    @PUT
    @Path("/{id}/order")
    @Timed
    @ExceptionMetered
    public Response setEnvPromotionOrderOnSystem(@PathParam("id") Long id, List<Long> envOrder, @Auth DefaultJwtCookiePrincipal user) {
        var loggedInUser = userDao.findBySystemIdentifier(user.getName())
                .orElseThrow(() -> new JaxrsNotAuthorizedException("Must be logged in to update systems"));

        var isAdmin = deployableSystemDao.isUserAdminOfSystem(loggedInUser.getId(), id);

        if (!isAdmin) {
            throw new JaxrsNotAuthorizedException("User is not an admin of this system");
        }

        var count = deployableSystemDao.updateEnvironmentPromotionOrder(id, StringUtils.join(envOrder, ','));

        if (count > 0) {
            auditAction(id, DeployableSystem.class, UPDATED);
        }

        return Response.accepted().build();
    }

    @DELETE
    @Path("/{id}")
    @Timed
    @ExceptionMetered
    @RolesAllowed({ "admin" })
    public Response deleteSystem(@PathParam("id") Long id) {
        var count = deployableSystemDao.deleteById(id);

        if (count > 0) {
            auditAction(id, DeployableSystem.class, DELETED);
        }

        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/user")
    @Timed
    @ExceptionMetered
    @RolesAllowed({ "admin" })
    public Response addUserToSystem(@PathParam("id") long systemId, SystemUser user) {
        deployableSystemDao.insertOrUpdateSystemUser(systemId, user.getUserId(), user.isAdmin());
        return Response.accepted().build();
    }

    @DELETE
    @Path("/{systemId}/users/{userId}")
    @Timed
    @ExceptionMetered
    @RolesAllowed({ "admin" })
    public Response removeUserFromSystem(@PathParam("systemId") long systemId, @PathParam("userId") long userId) {
        deployableSystemDao.deleteUserFromSystem(systemId, userId);
        return Response.noContent().build();
    }
}
