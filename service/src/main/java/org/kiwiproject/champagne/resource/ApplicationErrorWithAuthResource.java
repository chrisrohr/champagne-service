package org.kiwiproject.champagne.resource;

import java.util.OptionalInt;
import java.util.OptionalLong;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;
import org.kiwiproject.dropwizard.error.resource.ApplicationErrorResource;

/**
 * The purposes of this resource is to add authentication and authorization to the application errors endpoints. The
 * application errors library does not have a concept of auth as of this writing.
 */
@Path("/errors")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class ApplicationErrorWithAuthResource {

    private final ApplicationErrorResource delegate;

    public ApplicationErrorWithAuthResource(ApplicationErrorDao errorDao) {
        this(new ApplicationErrorResource(errorDao));
    }

    @VisibleForTesting
    ApplicationErrorWithAuthResource(ApplicationErrorResource delegate) {
        this.delegate = delegate;
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"admin"})
    @Timed
    @ExceptionMetered
    public Response getById(@PathParam("id") Long id) {
        return delegate.getById(OptionalLong.of(id));
    }

    @GET
    @RolesAllowed({"admin"})
    @Timed
    @ExceptionMetered
    public Response getErrors(@QueryParam("status") @DefaultValue("UNRESOLVED") String statusParam, @QueryParam("pageNumber") @DefaultValue("1") Integer pageNumber, @QueryParam("pageSize") @DefaultValue("25") Integer pageSize) {
        return delegate.getErrors(statusParam, OptionalInt.of(pageNumber), OptionalInt.of(pageSize));
    }

    @PUT
    @Path("/resolve/{id}")
    @RolesAllowed({"admin"})
    @Timed
    @ExceptionMetered
    public Response resolve(@PathParam("id") Long id) {
        return delegate.resolve(OptionalLong.of(id));
    }

    @PUT
    @Path("/resolve")
    @RolesAllowed({"admin"})
    @Timed
    @ExceptionMetered
    public Response resolveAllUnresolved() {
        return delegate.resolveAllUnresolved();
    }
}
