package org.kiwiproject.champagne.resource;

import static org.kiwiproject.champagne.util.DeployableSystems.getSystemIdOrNull;

import lombok.extern.slf4j.Slf4j;
import org.dhatim.dropwizard.jwt.cookie.authentication.CurrentPrincipal;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.dropwizard.error.ApplicationErrorThrower;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;

import java.util.Objects;

@Slf4j
public abstract class AuditableResource {

    private final AuditRecordDao auditRecordDao;
    private final ApplicationErrorThrower applicationErrorThrower;

    protected AuditableResource(AuditRecordDao auditRecordDao, ApplicationErrorDao applicationErrorDao) {
        this.auditRecordDao = auditRecordDao;
        this.applicationErrorThrower = ApplicationErrorThrower.builder()
                .errorDao(applicationErrorDao)
                .logger(LOG)
                .build();
    }

    protected void auditAction(long recordId, Class<?> recordClass, Action action) {
        var principal = CurrentPrincipal.get();

        if (Objects.isNull(principal)) {
            applicationErrorThrower.logAndSaveApplicationError("No user found in request, unable to track audit of action {} on {} with id {}", action, recordClass.getSimpleName(), recordId);
            return;
        }

        var auditRecord = AuditRecord.builder()
                .recordId(recordId)
                .recordType(recordClass.getSimpleName())
                .action(action)
                .userSystemIdentifier(principal.getName())
                .deployableSystemId(getSystemIdOrNull())
                .build();

        auditRecordDao.insertAuditRecord(auditRecord);
    }

}
