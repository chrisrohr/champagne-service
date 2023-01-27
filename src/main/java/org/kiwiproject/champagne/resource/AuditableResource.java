package org.kiwiproject.champagne.resource;

import java.util.Objects;

import org.dhatim.dropwizard.jwt.cookie.authentication.CurrentPrincipal;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.champagne.model.AuditRecord.Action;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AuditableResource {
    
    private AuditRecordDao auditRecordDao;

    protected AuditableResource(AuditRecordDao auditRecordDao) {
        this.auditRecordDao = auditRecordDao;
    }

    protected void auditAction(long recordId, Class<?> recordClass, Action action) {
        var principal = CurrentPrincipal.get();

        if (Objects.isNull(principal)) {
            LOG.warn("No user found in request, unable to track audit of action {} on {} with id {}", action, recordClass.getSimpleName(), recordId);
            return;
        }

        var auditRecord = AuditRecord.builder()
            .recordId(recordId)
            .recordType(recordClass.getSimpleName())
            .action(action)
            .userSystemIdentifier(principal.getName())
            .build();

        auditRecordDao.insertAuditRecord(auditRecord);
    }

}
