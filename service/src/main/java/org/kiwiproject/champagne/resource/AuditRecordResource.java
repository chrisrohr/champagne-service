package org.kiwiproject.champagne.resource;

import static org.kiwiproject.champagne.util.DeployableSystems.getSystemIdOrThrowBadRequest;
import static org.kiwiproject.search.KiwiSearching.zeroBasedOffset;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.spring.data.KiwiPage;

@Path("/audit")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@AllArgsConstructor
public class AuditRecordResource {

    private final AuditRecordDao auditRecordDao;

    @GET
    @RolesAllowed("admin")
    @Timed
    @ExceptionMetered
    public Response getPagedAudits(@QueryParam("pageNumber") @DefaultValue("1") int pageNumber,
                                     @QueryParam("pageSize") @DefaultValue("50") int pageSize) {

        var systemId = getSystemIdOrThrowBadRequest();
        var audits = auditRecordDao.findPagedAuditRecords(zeroBasedOffset(pageNumber, pageSize), pageSize, systemId);
        var totalCount = auditRecordDao.countAuditRecords(systemId);

        return Response.ok(KiwiPage.of(pageNumber, pageSize, totalCount, audits).usingOneAsFirstPage()).build();
    }
}
