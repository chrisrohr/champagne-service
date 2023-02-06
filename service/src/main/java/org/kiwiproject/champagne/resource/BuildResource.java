package org.kiwiproject.champagne.resource;

import static org.kiwiproject.search.KiwiSearching.zeroBasedOffset;

import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kiwiproject.champagne.dao.BuildDao;
import org.kiwiproject.champagne.model.Build;
import org.kiwiproject.json.JsonHelper;
import org.kiwiproject.spring.data.KiwiPage;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

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
    
        var offset = zeroBasedOffset(pageNumber, pageSize);

        var builds = buildDao.findPagedBuilds(offset, pageSize, componentIdentifierFilter, componentVersionFilter);
        var total = buildDao.countBuilds(componentIdentifierFilter, componentVersionFilter);

        return Response.ok(KiwiPage.of(pageNumber, pageSize, total, builds)).build();
    }

    @POST
    @Timed
    @ExceptionMetered
    public Response recordNewBuild(Build build) {
        buildDao.insertBuild(build, jsonHelper.toJson(build.getExtraDeploymentInfo()));
        return Response.accepted().build();
    }
}
