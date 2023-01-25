package org.kiwiproject.champagne.dao;

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
import org.kiwiproject.champagne.model.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.model.manualdeployment.ReleaseStage;
import org.kiwiproject.champagne.model.manualdeployment.TaskStatus;
import org.kiwiproject.champagne.dao.mappers.TaskStatusMapper;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

@DisplayName("TaskStatusDao")
@ExtendWith(SoftAssertionsExtension.class)
class TaskStatusDaoTest {
    
    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<TaskStatusDao> daoExtension = Jdbi3DaoExtension.<TaskStatusDao>builder()
            .daoType(TaskStatusDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private TaskStatusDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertTaskStatus {

        @Test
        void shouldInsertTaskStatusSuccessfully(SoftAssertions softly) {
            var beforeInsert = ZonedDateTime.now();

            var taskId = saveTestTaskRecord("Some task");
            var envId = saveTestEnvironmentRecord("TEST");

            var taskStatusToInsert = TaskStatus.builder()
                .environmentId(envId)
                .taskId(taskId)
                .status(DeploymentTaskStatus.PENDING)
                .build();

            var id = dao.insertTaskStatus(taskStatusToInsert);

            var taskStatuses = handle.select("select * from manual_deployment_task_statuses where id = ?", id)
                .map(new TaskStatusMapper())
                .list();

            assertThat(taskStatuses).hasSize(1);

            var status = first(taskStatuses);
            softly.assertThat(status.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance(softly, "createdAt", beforeInsert, status.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance(softly, "updatedAt", beforeInsert, status.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            softly.assertThat(status.getEnvironmentId()).isEqualTo(envId);
            softly.assertThat(status.getTaskId()).isEqualTo(taskId);
            softly.assertThat(status.getStatus()).isEqualTo(DeploymentTaskStatus.PENDING);
        }
    }

    @Nested
    class FindByTaskId {

        @Test
        void shouldReturnListOfTaskStatuses() {
            var taskId = saveTestTaskRecord("do it");
            saveTestTaskStatusRecord(DeploymentTaskStatus.PENDING, taskId);

            var statuses = dao.findByTaskId(taskId);
            assertThat(statuses)
                .extracting("status")
                .contains(DeploymentTaskStatus.PENDING);
        }

        @Test
        void shouldReturnEmptyListWhenNoStatusesFound() {
            var statuses = dao.findByTaskId(42L);
            assertThat(statuses).isEmpty();
        }

    }

    @Nested
    class UpdateStatus {

        @Test
        void shouldUpdateTheStatusOfAGivenRecord() {
            var statusId = saveTestTaskStatusRecord(DeploymentTaskStatus.PENDING);

            dao.updateStatus(statusId, DeploymentTaskStatus.COMPLETE);

            var taskStatuses = handle.select("select * from manual_deployment_task_statuses where id = ?", statusId)
                .map(new TaskStatusMapper())
                .list();

            assertThat(taskStatuses).hasSize(1);

            var status = first(taskStatuses);
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

    private long saveTestTaskRecord(String summary) {
        var releaseId = saveTestReleaseRecord("42");
        return saveTestTaskRecord(summary, releaseId);
    }

    private long saveTestTaskRecord(String summary, long releaseId) {
        handle.execute("insert into manual_deployment_tasks (manual_deployment_task_release_id, stage, summary, component) values (?, ?, ?, ?)", 
            releaseId, ReleaseStage.POST, summary, "component");

        return handle.select("select * from manual_deployment_tasks where summary = ?", summary)
                .mapToMap()
                .findFirst()
                .map(row -> (long) row.get("id"))
                .orElseThrow();
    }

    private long saveTestTaskStatusRecord(DeploymentTaskStatus status) {
        var taskId = saveTestTaskRecord("Some Task");
        return saveTestTaskStatusRecord(status, taskId);
    }

    private long saveTestTaskStatusRecord(DeploymentTaskStatus status, long taskId) {
        var envId = saveTestEnvironmentRecord("DEV");

        handle.execute("insert into manual_deployment_task_statuses (manual_deployment_task_id, deployment_environment_id, status) values (?, ?, ?)",
            taskId, envId, status);

        return handle.select("select * from manual_deployment_task_statuses where status = ?", status)
            .mapToMap()
            .findFirst()
            .map(row -> (long) row.get("id"))
            .orElseThrow();
    }
}
