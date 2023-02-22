package org.kiwiproject.champagne.resource;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dhatim.dropwizard.jwt.cookie.authentication.DefaultJwtCookiePrincipal;
import org.dhatim.dropwizard.jwt.cookie.authentication.JwtCookiePrincipal;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.jaxrs.exception.JaxrsNotAuthorizedException;

import static java.util.Objects.isNull;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource for various authentication based endpoints
 */
@Path("/auth")
@AllArgsConstructor
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final UserDao userDao;

    @POST
    @Path("/login")
    @Timed
    @ExceptionMetered
    public Response login(@Context ContainerRequestContext context, LoginRequest request) {

        // TODO: Need to actually check password or valid login not just user lookup
        var user = userDao.findBySystemIdentifier(request.getUsername()).orElse(null);

        if (isNull(user)) {
            throw new JaxrsNotAuthorizedException("Invalid login");
        }

        List<String> roles = user.isAdmin() ? List.of("admin") : List.of();

        var principal = new DefaultJwtCookiePrincipal(user.getSystemIdentifier(), false, roles, null);
        principal.addInContext(context);

        return Response.ok(user).build();
    }

    @DELETE
    @Path("/logout")
    @Timed
    @ExceptionMetered
    public Response logout(@Context ContainerRequestContext context) {
        JwtCookiePrincipal.removeFromContext(context);
        return Response.noContent().build();
    }

    @Getter
    static class LoginRequest {
        private String username;
    }
}
