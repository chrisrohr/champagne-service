package org.kiwiproject.champagne.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;
import static org.kiwiproject.jaxrs.KiwiStandardResponses.standardGetResponse;
import static org.kiwiproject.search.KiwiSearching.zeroBasedOffset;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import org.dhatim.dropwizard.jwt.cookie.authentication.DefaultJwtCookiePrincipal;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.spring.data.KiwiPage;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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

@Path("/users")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@PermitAll
public class UserResource extends AuditableResource {

    private final UserDao userDao;

    public UserResource(UserDao userDao, AuditRecordDao auditRecordDao) {
        super(auditRecordDao);

        this.userDao = userDao;
    }

    @GET
    @Timed
    @ExceptionMetered
    public Response listUsers(@QueryParam("pageNumber") @DefaultValue("1") int pageNumber, 
                              @QueryParam("pageSize") @DefaultValue("25") int pageSize) {

        var offset = zeroBasedOffset(pageNumber, pageSize);

        var users = userDao.findPagedUsers(offset, pageSize);
        var total = userDao.countUsers();

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
}
