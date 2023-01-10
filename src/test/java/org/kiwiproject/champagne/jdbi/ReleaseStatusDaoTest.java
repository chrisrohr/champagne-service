package org.kiwiproject.champagne.jdbi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.util.DateTimeTestHelper.assertTimeDifferenceWithinTolerance;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.core.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.core.manualdeployment.ReleaseStatus;
import org.kiwiproject.champagne.jdbi.mappers.ReleaseStatusMapper;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

@DisplayName("ReleaseStatusDao")
@ExtendWith(SoftAssertionsExtension.class)
class ReleaseStatusDaoTest {
    
    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<ReleaseStatusDao> daoExtension = Jdbi3DaoExtension.<ReleaseStatusDao>builder()
            .daoType(ReleaseStatusDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private ReleaseStatusDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertReleaseStatus {

        @Test
        void shouldInsertReleaseStatusSuccessfully(SoftAssertions softly) {
            var beforeInsert = ZonedDateTime.now();

            var releaseId = saveTestReleaseRecord("42");
            var envId = saveTestEnvironmentRecord("TEST");

            var releaseStatusToInsert = ReleaseStatus.builder()
                .environmentId(envId)
                .releaseId(releaseId)
                .status(DeploymentTaskStatus.PENDING)
                .build();

            var id = dao.insertReleaseStatus(releaseStatusToInsert);

            var releaseStatuses = handle.select("select * from manual_deployment_task_release_statuses where id = ?", id)
                .map(new ReleaseStatusMapper())
                .list();

            assertThat(releaseStatuses).hasSize(1);

            var status = first(releaseStatuses);
            softly.assertThat(status.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance(softly, "createdAt", beforeInsert, status.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance(softly, "updatedAt", beforeInsert, status.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            softly.assertThat(status.getEnvironmentId()).isEqualTo(envId);
            softly.assertThat(status.getReleaseId()).isEqualTo(releaseId);
            softly.assertThat(status.getStatus()).isEqualTo(DeploymentTaskStatus.PENDING);
        }
    }

    @Nested
    class FindByReleaseId {

        @Test
        void shouldReturnListOfReleaseStatuses() {
            var releaseId = saveTestReleaseRecord("42");
            saveTestReleaseStatusRecord(DeploymentTaskStatus.PENDING, releaseId);

            var statuses = dao.findByReleaseId(releaseId);
            assertThat(statuses)
                .extracting("status")
                .contains(DeploymentTaskStatus.PENDING);
        }

        @Test
        void shouldReturnEmptyListWhenNoStatusesFound() {
            var statuses = dao.findByReleaseId(42L);
            assertThat(statuses).isEmpty();
        }

    }

    @Nested
    class UpdateStatus {

        @Test
        void shouldUpdateTheStatusOfAGivenRecord() {
            var statusId = saveTestReleaseStatusRecord(DeploymentTaskStatus.PENDING);

            dao.updateStatus(statusId, DeploymentTaskStatus.COMPLETE);

            var releaseStatuses = handle.select("select * from manual_deployment_task_release_statuses where id = ?", statusId)
                .map(new ReleaseStatusMapper())
                .list();

            assertThat(releaseStatuses).hasSize(1);

            var status = first(releaseStatuses);
            assertThat(status.getStatus()).isEqualTo(DeploymentTaskStatus.COMPLETE);
        }
    }

    private long saveTestReleaseRecord(String releaseNumber) {
        handle.execute("insert into manual_deployment_task_releases (release_number) values (?)", releaseNumber);

        return handle.select("select * from manual_deployment_task_releases where release_number = ?", releaseNumber)
                .mapToMap()
                .findFirst()
                .map(row -> (long) row.get("id"))
                .orElseThrow();
    }

    private long saveTestEnvironmentRecord(String env) {
        var userId = saveTestUserRecord("jdoe");

        handle.execute("insert into deployment_environments (environment_name, created_by, updated_by) values (?, ?, ?)", env, userId, userId);

        return handle.select("select * from deployment_environments where environment_name = ?", env)
                .mapToMap()
                .findFirst()
                .map(row -> (long) row.get("id"))
                .orElseThrow();
    }

    private long saveTestUserRecord(String systemIdentifier) {
        handle.execute("insert into users (system_identifier, first_name, last_name, display_name) values (?, ?, ?, ?)",
                systemIdentifier, "John", "Doe", "John Doe");

        return handle.select("select * from users where system_identifier = ?", systemIdentifier)
                .mapToMap()
                .findFirst()
                .map(row -> (long) row.get("id"))
                .orElseThrow();
    }

    private long saveTestReleaseStatusRecord(DeploymentTaskStatus status) {
        var releaseId = saveTestReleaseRecord("42");
        return saveTestReleaseStatusRecord(status, releaseId);
    }

    private long saveTestReleaseStatusRecord(DeploymentTaskStatus status, long releaseId) {
        var envId = saveTestEnvironmentRecord("DEV");

        handle.execute("insert into manual_deployment_task_release_statuses (manual_deployment_task_release_id, deployment_environment_id, status) values (?, ?, ?)",
            releaseId, envId, status);

        return handle.select("select * from manual_deployment_task_release_statuses where status = ?", status)
            .mapToMap()
            .findFirst()
            .map(row -> (long) row.get("id"))
            .orElseThrow();
    }
}
