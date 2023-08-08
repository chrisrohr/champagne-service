package org.kiwiproject.champagne.resource;

import static java.util.Objects.isNull;

import java.util.List;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dhatim.dropwizard.jwt.cookie.authentication.DefaultJwtCookiePrincipal;
import org.dhatim.dropwizard.jwt.cookie.authentication.JwtCookiePrincipal;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.jaxrs.exception.JaxrsNotAuthorizedException;

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
