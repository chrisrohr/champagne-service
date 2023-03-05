package org.kiwiproject.champagne.resource;

import static java.util.Objects.isNull;
import static org.kiwiproject.champagne.util.DeployableSystems.getSystemIdOrThrowBadRequest;
import static org.kiwiproject.search.KiwiSearching.zeroBasedOffset;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import org.kiwiproject.champagne.dao.BuildDao;
import org.kiwiproject.champagne.model.Build;
import org.kiwiproject.json.JsonHelper;
import org.kiwiproject.spring.data.KiwiPage;

import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/build")
@Produces(MediaType.APPLICATION_JSON)
public class BuildResource {
    
    private final BuildDao buildDao;
    private final JsonHelper jsonHelper;

    public BuildResource(BuildDao buildDao, JsonHelper jsonHelper) {
        this.buildDao = buildDao;
        this.jsonHelper = jsonHelper;
    }

    @GET
    @Timed
    @ExceptionMetered
    @PermitAll
    public Response listBuilds(@QueryParam("pageNumber") @DefaultValue("1") int pageNumber, 
                              @QueryParam("pageSize") @DefaultValue("50") int pageSize,
                              @QueryParam("componentIdentifierFilter") String componentIdentifierFilter,
                              @QueryParam("componentVersionFilter") String componentVersionFilter) {

        var systemId = getSystemIdOrThrowBadRequest();
        var offset = zeroBasedOffset(pageNumber, pageSize);

        var builds = buildDao.findPagedBuilds(offset, pageSize, systemId, componentIdentifierFilter, componentVersionFilter);
        var total = buildDao.countBuilds(systemId, componentIdentifierFilter, componentVersionFilter);

        return Response.ok(KiwiPage.of(pageNumber, pageSize, total, builds)).build();
    }

    @POST
    @Timed
    @ExceptionMetered
    public Response recordNewBuild(Build build) {

        if (isNull(build.getDeployableSystemId())) {
            var systemId = getSystemIdOrThrowBadRequest();
            build = build.withDeployableSystemId(systemId);
        }

        buildDao.insertBuild(build, jsonHelper.toJson(build.getExtraDeploymentInfo()));
        return Response.accepted().build();
    }
}
