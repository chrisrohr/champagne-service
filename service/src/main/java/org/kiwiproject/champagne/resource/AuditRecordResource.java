package org.kiwiproject.champagne.resource;

import static org.kiwiproject.champagne.util.DeployableSystems.getSystemIdOrThrowBadRequest;
import static org.kiwiproject.search.KiwiSearching.zeroBasedOffset;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import lombok.AllArgsConstructor;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.spring.data.KiwiPage;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
