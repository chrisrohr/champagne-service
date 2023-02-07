package org.kiwiproject.champagne.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.champagne.dao.AuditRecordDao;

import java.time.Instant;

@DisplayName("CleanOutAuditsJob")
class CleanOutAuditsJobTest {

    private AuditRecordDao auditRecordDao;
    private CleanOutAuditsJob job;

    @BeforeEach
    void setUp() {
        auditRecordDao = mock(AuditRecordDao.class);
        job = new CleanOutAuditsJob(auditRecordDao, 100);
    }

    @Test
    void shouldAttemptToDeleteOldAudits() {
        when(auditRecordDao.deleteAuditRecordsOlderThan(any(Instant.class))).thenReturn(1);

        var now = Instant.now();
        job.run();

        verify(auditRecordDao).deleteAuditRecordsOlderThan(argThat(i -> i.isAfter(now.minusMillis(150))));
    }
}
