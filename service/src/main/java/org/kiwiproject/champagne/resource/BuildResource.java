package org.kiwiproject.champagne.resource;

import static java.util.Objects.isNull;
import static org.kiwiproject.champagne.util.DeployableSystems.getSystemIdOrThrowBadRequest;
import static org.kiwiproject.search.KiwiSearching.zeroBasedOffset;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.kiwiproject.champagne.dao.BuildDao;
import org.kiwiproject.champagne.model.Build;
import org.kiwiproject.json.JsonHelper;
import org.kiwiproject.spring.data.KiwiPage;

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
                              @QueryParam("componentFilter") String componentFilter) {

        var systemId = getSystemIdOrThrowBadRequest();
        var offset = zeroBasedOffset(pageNumber, pageSize);

        var builds = buildDao.findPagedBuilds(offset, pageSize, systemId, componentFilter);
        var total = buildDao.countBuilds(systemId, componentFilter);

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
