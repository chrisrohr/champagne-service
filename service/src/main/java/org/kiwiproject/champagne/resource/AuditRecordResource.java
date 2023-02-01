package org.kiwiproject.champagne.resource;

import static org.kiwiproject.search.KiwiSearching.zeroBasedOffset;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.spring.data.KiwiPage;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

import lombok.AllArgsConstructor;

@Path("/audit")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@AllArgsConstructor
public class AuditRecordResource {

    private final AuditRecordDao auditRecordDao;

    @GET
    @Timed
    @ExceptionMetered
    public Response getPagedAudits(@QueryParam("pageNumber") @DefaultValue("1") int pageNumber,
                                     @QueryParam("pageSize") @DefaultValue("50") int pageSize) {

        var audits = auditRecordDao.findPagedAuditRecords(zeroBasedOffset(pageNumber, pageSize), pageSize);
        var totalCount = auditRecordDao.countAuditRecords();

        return Response.ok(KiwiPage.of(pageNumber, pageSize, totalCount, audits).usingOneAsFirstPage()).build();
    }
}
