package org.kiwiproject.champagne.resource;

import static org.kiwiproject.champagne.util.DeployableSystems.checkUserAdminOfSystem;
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
    @Timed
    @ExceptionMetered
    public Response getPagedAuditsForSystem(@QueryParam("pageNumber") @DefaultValue("1") int pageNumber,
                                     @QueryParam("pageSize") @DefaultValue("50") int pageSize) {

        checkUserAdminOfSystem();

        var systemId = getSystemIdOrThrowBadRequest();

        var audits = auditRecordDao.findPagedAuditRecordsForSystem(zeroBasedOffset(pageNumber, pageSize), pageSize, systemId);
        var totalCount = auditRecordDao.countAuditRecordsForSystem(systemId);

        return Response.ok(KiwiPage.of(pageNumber, pageSize, totalCount, audits).usingOneAsFirstPage()).build();
    }

    @GET
    @Path("/all")
    @RolesAllowed("admin")
    @Timed
    @ExceptionMetered
    public Response getPagedAudits(@QueryParam("pageNumber") @DefaultValue("1") int pageNumber,
                                   @QueryParam("pageSize") @DefaultValue("50") int pageSize) {

        var audits = auditRecordDao.findPagedAuditRecords(zeroBasedOffset(pageNumber, pageSize), pageSize);
        var totalCount = auditRecordDao.countAuditRecords();

        return Response.ok(KiwiPage.of(pageNumber, pageSize, totalCount, audits).usingOneAsFirstPage()).build();
    }
}
