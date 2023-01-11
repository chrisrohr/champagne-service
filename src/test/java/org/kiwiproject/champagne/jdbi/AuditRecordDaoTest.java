package org.kiwiproject.champagne.jdbi;

import static org.assertj.core.api.Assertions.assertThat;

import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.core.AuditRecord;
import org.kiwiproject.champagne.jdbi.mappers.AuditRecordMapper;
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
            var auditRecordToInsert = AuditRecord.builder()
                    .action(AuditRecord.Action.CREATED)
                    .userSystemIdentifier("jdoe")
                    .recordType("User")
                    .recordId(1L)
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
    class FindPagedAuditRecords {

        @Test
        void shouldReturnListOfAuditRecords() {
            saveTestAuditRecord();

            var audits = dao.findPagedAuditRecords(0, 10);
            assertThat(audits).hasSize(1);
        }

        @Test
        void shouldReturnEmptyListWhenNoAuditRecordsFound() {
            saveTestAuditRecord();

            var audits = dao.findPagedAuditRecords(10, 10);
            assertThat(audits).isEmpty();
        }
    }

    @Nested
    class CountAuditRecords {

        @Test
        void shouldReturnCountOfAuditRecords() {
            saveTestAuditRecord();

            var count = dao.countAuditRecords();
            assertThat(count).isOne();
        }

        @Test
        void shouldReturnZeroWhenNoAuditRecordsFound() {
            var count = dao.countAuditRecords();
            assertThat(count).isZero();
        }
    }

    private void saveTestAuditRecord() {
        handle.execute("insert into audit_records (user_system_identifier, action, record_type, record_id) values (?, ?, ?, ?)",
                "jdoe", "UPDATED", "Task", 1L);
    }
}
