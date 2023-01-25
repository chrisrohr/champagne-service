package org.kiwiproject.champagne.resource;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import lombok.AllArgsConstructor;
import org.dhatim.dropwizard.jwt.cookie.authentication.DefaultJwtCookiePrincipal;
import org.dhatim.dropwizard.jwt.cookie.authentication.JwtCookiePrincipal;
import org.kiwiproject.champagne.jdbi.UserDao;
import org.kiwiproject.jaxrs.exception.JaxrsNotAuthorizedException;

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
    public Response login(@Context ContainerRequestContext context, String name) {

        // TODO: Need to actually check password or valid login not just user lookup
        var userOptional = userDao.findBySystemIdentifier(name);

        userOptional.map(u -> {
            var principal = new DefaultJwtCookiePrincipal(u.getSystemIdentifier());
            principal.addInContext(context);
            return principal;
        }).orElseThrow(() -> new JaxrsNotAuthorizedException("Invalid login"));

        return Response.ok().build();
    }

    @DELETE
    @Path("/logout")
    @Timed
    @ExceptionMetered
    public Response logout(@Context ContainerRequestContext context) {
        JwtCookiePrincipal.removeFromContext(context);
        return Response.noContent().build();
    }
}
