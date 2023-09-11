package org.kiwiproject.champagne.resource;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;
import static org.kiwiproject.champagne.util.DeployableSystems.checkUserAdminOfSystem;
import static org.kiwiproject.champagne.util.DeployableSystems.getSystemIdOrThrowBadRequest;
import static org.kiwiproject.jaxrs.KiwiStandardResponses.standardGetResponse;
import static org.kiwiproject.search.KiwiSearching.zeroBasedOffset;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import org.dhatim.dropwizard.jwt.cookie.authentication.DefaultJwtCookiePrincipal;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.DeployableSystemDao;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.champagne.model.UserInSystem;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;
import org.kiwiproject.spring.data.KiwiPage;

@Path("/users")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@PermitAll
public class UserResource extends AuditableResource {

    private final UserDao userDao;
    private final DeployableSystemDao deployableSystemDao;

    public UserResource(UserDao userDao, DeployableSystemDao deployableSystemDao, AuditRecordDao auditRecordDao, ApplicationErrorDao errorDao) {
        super(auditRecordDao, errorDao);

        this.userDao = userDao;
        this.deployableSystemDao = deployableSystemDao;
    }

    @GET
    @Path("/all")
    @RolesAllowed("admin")
    @Timed
    @ExceptionMetered
    public Response listUsers(@QueryParam("pageNumber") @DefaultValue("1") int pageNumber, 
                              @QueryParam("pageSize") @DefaultValue("25") int pageSize) {

        var offset = zeroBasedOffset(pageNumber, pageSize);

        var users = userDao.findPagedUsers(offset, pageSize);
        users = users.stream().map(user -> user.withSystems(deployableSystemDao.findDeployableSystemsForUser(user.getId()))).toList();
        var total = userDao.countUsers();

        var page = KiwiPage.of(pageNumber, pageSize, total, users);
        return Response.ok(page).build();
    }

    @GET
    @Timed
    @ExceptionMetered
    public Response listUsersInSystem(@QueryParam("pageNumber") @DefaultValue("1") int pageNumber,
                              @QueryParam("pageSize") @DefaultValue("25") int pageSize) {

        var systemId = getSystemIdOrThrowBadRequest();
        var offset = zeroBasedOffset(pageNumber, pageSize);

        var users = userDao.findPagedUsersInSystem(systemId, offset, pageSize);
        var total = userDao.countUsersInSystem(systemId);

        var page = KiwiPage.of(pageNumber, pageSize, total, users);
        return Response.ok(page).build();
    }

    @POST
    @RolesAllowed("admin")
    @Timed
    @ExceptionMetered
    public Response addUser(@NotNull @Valid User user) {
        var userId = userDao.insertUser(user);

        auditAction(userId, User.class, Action.CREATED);

        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    @Timed
    @ExceptionMetered
    public Response deleteUser(@PathParam("id") long id) {
        var deletedCount = userDao.deleteUser(id);

        if (deletedCount > 0) {
            auditAction(id, User.class, Action.DELETED);
        }

        return Response.noContent().build();
    }

    @GET
    @Path("/current")
    @Timed
    @ExceptionMetered
    public Response getLoggedInUser(@Auth DefaultJwtCookiePrincipal principal) {
        var loggedInUser = userDao.findBySystemIdentifier(principal.getName());

        return standardGetResponse(loggedInUser, "Unable to find logged in user");
    }

    @PUT
    @RolesAllowed("admin")
    @Timed
    @ExceptionMetered
    public Response updateUser(@NotNull @Valid User userToUpdate) {
        requireNotNull(userToUpdate.getId(), "Id is required to update a user");

        var updatedCount = userDao.updateUser(userToUpdate);

        if (updatedCount > 0) {
            auditAction(userToUpdate.getId(), User.class, Action.UPDATED);
        }

        return Response.accepted().build();
    }

    @DELETE
    @Path("/system/{id}")
    @Timed
    @ExceptionMetered
    public Response removeUserFromSystem(@PathParam("id") long id) {
        checkUserAdminOfSystem();

        var systemId = getSystemIdOrThrowBadRequest();
        var removeCount = userDao.removeUserFromSystem(id, systemId);
        if (removeCount > 0) {
            auditAction(id, UserInSystem.class, Action.DELETED);
        }

        return Response.noContent().build();
    }

    @PUT
    @Path("/system/{id}")
    @Timed
    @ExceptionMetered
    public Response updateSystemAdminStatus(@PathParam("id") long id, SystemAdminRequest request) {
        checkUserAdminOfSystem();

        var systemId = getSystemIdOrThrowBadRequest();
        var updateCount = 0;

        if (request.admin) {
            updateCount = userDao.makeUserAdminInSystem(id, systemId);
        } else {
            updateCount = userDao.makeUserNonAdminInSystem(id, systemId);
        }

        if (updateCount > 0) {
            auditAction(id, UserInSystem.class, Action.UPDATED);
        }

        return Response.accepted().build();
    }

    public record SystemAdminRequest(boolean admin) {}

    @POST
    @Path("/system/{systemId}/{id}")
    @RolesAllowed("admin")
    @Timed
    @ExceptionMetered
    public Response addUserToSystem(@PathParam("systemId") long systemId, @PathParam("id") long id) {
        var insertCount = userDao.addUserToSystem(id, systemId);

        if (insertCount > 0) {
            auditAction(id, User.class, Action.UPDATED);
        }

        return Response.accepted().build();
    }
}
