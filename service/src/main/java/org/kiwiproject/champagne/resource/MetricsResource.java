package org.kiwiproject.champagne.resource;

import static org.kiwiproject.champagne.util.DeployableSystems.getSystemIdOrThrowBadRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.kiwiproject.champagne.dao.BuildDao;

@Path("/metrics")
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource {

    private final BuildDao buildDao;

    public MetricsResource(BuildDao buildDao) {
        this.buildDao = buildDao;
    }

    @GET
    @Timed
    @ExceptionMetered
    @PermitAll
    public Response getMetrics() {
        var systemId = getSystemIdOrThrowBadRequest();

        var endOfToday = LocalDate.now().atTime(LocalTime.MAX);
        var oneMonthAgo = endOfToday.minusDays(30).truncatedTo(ChronoUnit.DAYS);

        long thisMonthCount = buildDao.countBuildsInSystemInRange(systemId, oneMonthAgo.toInstant(ZoneOffset.UTC), endOfToday.toInstant(ZoneOffset.UTC));

        var endOfLastMonth = oneMonthAgo.minusSeconds(1);
        var startOfPreviousMonth = endOfLastMonth.minusDays(30).truncatedTo(ChronoUnit.DAYS);

        long lastMonthCount = buildDao.countBuildsInSystemInRange(systemId, startOfPreviousMonth.toInstant(ZoneOffset.UTC), endOfLastMonth.toInstant(ZoneOffset.UTC));

        return Response.ok(Map.of("builds", Map.of("current", thisMonthCount, "previous", lastMonthCount))).build();
    }
}
