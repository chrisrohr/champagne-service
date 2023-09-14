package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.champagne.util.TestObjects.insertAuditRecord;
import static org.kiwiproject.champagne.util.TestObjects.insertDeployableSystem;

import java.time.Instant;

import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.mappers.AuditRecordMapper;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

@DisplayName("AuditRecordDao")
class AuditRecordDaoTest {

    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<AuditRecordDao> daoExtension = Jdbi3DaoExtension.<AuditRecordDao>builder()
            .daoType(AuditRecordDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private AuditRecordDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertAuditRecord {

        @Test
        void shouldInsertAuditRecordSuccessfully() {
            insertDeployableSystem(handle, "kiwi");

            var auditRecordToInsert = AuditRecord.builder()
                    .action(AuditRecord.Action.CREATED)
                    .userSystemIdentifier("jdoe")
                    .recordType("User")
                    .recordId(1L)
                    .deployableSystemId(1L)
                    .build();

            var id = dao.insertAuditRecord(auditRecordToInsert);

            var audit = handle.select("select * from audit_records where id = ?", id)
                    .map(new AuditRecordMapper())
                    .findFirst()
                    .orElseThrow();

            assertThat(audit.getId()).isEqualTo(id);
            assertThat(audit.getTimestamp()).isNotNull();

            assertThat(audit)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "timestamp")
                    .isEqualTo(auditRecordToInsert);
        }
    }

    @Nested
    class FindPagedAuditRecordsForSystem {

        @Test
        void shouldReturnListOfAuditRecords() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            insertAuditRecord(handle, systemId);

            var audits = dao.findPagedAuditRecordsForSystem(0, 10, systemId);
            assertThat(audits).hasSize(1);
        }

        @Test
        void shouldReturnEmptyListWhenNoAuditRecordsFound() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            insertAuditRecord(handle, systemId);

            var audits = dao.findPagedAuditRecordsForSystem(10, 10, systemId);
            assertThat(audits).isEmpty();
        }
    }

    @Nested
    class CountAuditRecordsForSystem {

        @Test
        void shouldReturnCountOfAuditRecords() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            insertAuditRecord(handle, systemId);

            var count = dao.countAuditRecordsForSystem(systemId);
            assertThat(count).isOne();
        }

        @Test
        void shouldReturnZeroWhenNoAuditRecordsFound() {
            var count = dao.countAuditRecordsForSystem(1L);
            assertThat(count).isZero();
        }
    }

    @Nested
    class FindPagedAuditRecords {

        @Test
        void shouldReturnListOfAuditRecords() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            insertAuditRecord(handle, systemId);

            var audits = dao.findPagedAuditRecords(0, 10);
            assertThat(audits).hasSize(1);
        }

        @Test
        void shouldReturnEmptyListWhenNoAuditRecordsFound() {
            var audits = dao.findPagedAuditRecords(10, 10);
            assertThat(audits).isEmpty();
        }
    }

    @Nested
    class CountAuditRecords {

        @Test
        void shouldReturnCountOfAuditRecords() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            insertAuditRecord(handle, systemId);

            var count = dao.countAuditRecords();
            assertThat(count).isOne();
        }

        @Test
        void shouldReturnZeroWhenNoAuditRecordsFound() {
            var count = dao.countAuditRecords();
            assertThat(count).isZero();
        }
    }

    @Nested
    class DeleteAuditRecordsOlderThan {

        @Test
        void shouldDeleteFoundRecords() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            insertAuditRecord(handle, systemId);

            var deleteDate = Instant.now().plusSeconds(60);

            var deletedCount = dao.deleteAuditRecordsOlderThan(deleteDate);
            assertThat(deletedCount).isOne();
        }
    }
}
