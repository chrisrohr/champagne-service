package org.kiwiproject.champagne.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.kiwiproject.search.KiwiSearching.zeroBasedOffset;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

import org.kiwiproject.champagne.model.User;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.spring.data.KiwiPage;

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
    @Timed
    @ExceptionMetered
    public Response addUser(@NotNull @Valid User user) {
        var userId = userDao.insertUser(user);

        auditAction(userId, User.class, Action.CREATED);

        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    @Timed
    @ExceptionMetered
    public Response deleteUser(@PathParam("id") long id) {
        userDao.deleteUser(id);

        auditAction(id, User.class, Action.DELETED);

        return Response.noContent().build();
    }

    // TODO: Add endpoint to update a user
    // TODO: Add endpoint to search for users in a 3rd party system (need discussions on how to implement this)
}
