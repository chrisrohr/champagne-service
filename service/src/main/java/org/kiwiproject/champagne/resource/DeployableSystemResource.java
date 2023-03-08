package org.kiwiproject.champagne.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.kiwiproject.champagne.model.AuditRecord.Action.CREATED;
import static org.kiwiproject.champagne.model.AuditRecord.Action.DELETED;
import static org.kiwiproject.champagne.model.AuditRecord.Action.UPDATED;
import static org.kiwiproject.search.KiwiSearching.zeroBasedOffset;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import org.apache.commons.lang3.StringUtils;
import org.dhatim.dropwizard.jwt.cookie.authentication.DefaultJwtCookiePrincipal;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.DeployableSystemDao;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.champagne.model.DeployableSystem;
import org.kiwiproject.champagne.model.DeployableSystem.SystemUser;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;
import org.kiwiproject.jaxrs.exception.JaxrsNotAuthorizedException;
import org.kiwiproject.spring.data.KiwiPage;

import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
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
import javax.ws.rs.core.Response;

@Path("/systems")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@PermitAll
public class DeployableSystemResource extends AuditableResource {

    private final DeployableSystemDao deployableSystemDao;
    private final UserDao userDao;

    public DeployableSystemResource(DeployableSystemDao deployableSystemDao, UserDao userDao, AuditRecordDao auditRecordDao, ApplicationErrorDao errorDao) {
        super(auditRecordDao, errorDao);

        this.deployableSystemDao = deployableSystemDao;
        this.userDao = userDao;
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
                .map(system -> system.withUsers(deployableSystemDao.findUsersForSystem(system.getId())))
                .toList();

        var total = deployableSystemDao.countDeployableSystems();

        var page = KiwiPage.of(pageNumber, pageSize, total, systems);
        return Response.ok(page).build();
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
    @Path("/{id}/users")
    @Timed
    @ExceptionMetered
    @RolesAllowed({ "admin" })
    public Response addUsersToSystem(@PathParam("id") long systemId, List<SystemUser> users) {
        users.forEach(user -> deployableSystemDao.insertOrUpdateSystemUser(systemId, user.getUserId(), user.isAdmin()));
        return Response.accepted().build();
    }
}
