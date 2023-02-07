package org.kiwiproject.champagne.job;

import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.champagne.dao.AuditRecordDao;

import java.time.Instant;

/**
 * TODO: There are plans for an AutoCleaner utility to be added to KiwiProject, when this happens, this Job can
 *       be replaced by it.
 */
@Slf4j
public class CleanOutAuditsJob implements Runnable {

    private final AuditRecordDao auditRecordDao;
    private final long maxRetainInMillis;

    public CleanOutAuditsJob(AuditRecordDao auditRecordDao, long maxRetainInMillis) {
        this.auditRecordDao = auditRecordDao;
        this.maxRetainInMillis = maxRetainInMillis;
    }

    @Override
    public void run() {
        LOG.debug("Running Audit Record Cleanup");

        var now = Instant.now();
        var maxRetainInstant = now.minusMillis(maxRetainInMillis);

        LOG.debug("Deleting Audit Records older than {}", maxRetainInstant);

        var deletedCount = auditRecordDao.deleteAuditRecordsOlderThan(maxRetainInstant);

        LOG.debug("Deleted {} Audit Records", deletedCount);
    }
}
