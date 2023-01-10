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
import org.kiwiproject.champagne.core.manualdeployment.ReleaseStage;
import org.kiwiproject.champagne.core.manualdeployment.Task;
import org.kiwiproject.champagne.jdbi.mappers.TaskMapper;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

@DisplayName("TaskDao")
@ExtendWith(SoftAssertionsExtension.class)
class TaskDaoTest {
    
    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<TaskDao> daoExtension = Jdbi3DaoExtension.<TaskDao>builder()
            .daoType(TaskDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private TaskDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertTask {

        @Test
        void shouldInsertTaskSuccessfully(SoftAssertions softly) {
            var beforeInsert = ZonedDateTime.now();

            var releaseId = saveTestReleaseRecord("42");

            var taskToInsert = Task.builder()
                .releaseId(releaseId)
                .stage(ReleaseStage.PRE)
                .summary("Do the things")
                .description("No really do all of it")
                .component("The best component")
                .build();

            var id = dao.insertTask(taskToInsert);

            var tasks = handle.select("select * from manual_deployment_tasks where id = ?", id)
                .map(new TaskMapper())
                .list();

            assertThat(tasks).hasSize(1);

            var task = first(tasks);
            softly.assertThat(task.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance(softly, "createdAt", beforeInsert, task.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance(softly, "updatedAt", beforeInsert, task.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            softly.assertThat(task.getReleaseId()).isEqualTo(releaseId);
            softly.assertThat(task.getStage()).isEqualTo(ReleaseStage.PRE);
            softly.assertThat(task.getSummary()).isEqualTo("Do the things");
            softly.assertThat(task.getDescription()).isEqualTo("No really do all of it");
            softly.assertThat(task.getComponent()).isEqualTo("The best component");

        }
    }

    @Nested
    class FindByReleaseId {

        @Test
        void shouldReturnListOfTasks() {
            var releaseId = saveTestReleaseRecord("42");
            saveTestTaskRecord("Sample", releaseId);

            var tasks = dao.findByReleaseId(releaseId);
            assertThat(tasks)
                .extracting("summary")
                .contains("Sample");
        }

        @Test
        void shouldReturnEmptyListWhenNoReleasesFound() {
            var tasks = dao.findByReleaseId(42L);
            assertThat(tasks).isEmpty();
        }
    }

    @Nested
    class UpdateTask {

        @Test
        void shouldUpdateTask() {
            var taskId = saveTestTaskRecord("Old value");

            var taskToUpdate = Task.builder()
                .id(taskId)
                .stage(ReleaseStage.PRE)
                .summary("New value")
                .description("new description")
                .component("even better component")
                .build();

            dao.updateTask(taskToUpdate);

            var task = handle.select("select * from manual_deployment_tasks where id = ?", taskId)
                .map(new TaskMapper())
                .first();

            assertThat(task.getStage()).isEqualTo(ReleaseStage.PRE);
            assertThat(task.getSummary()).isEqualTo("New value");
            assertThat(task.getDescription()).isEqualTo("new description");
            assertThat(task.getComponent()).isEqualTo("even better component");
        }
    }

    @Nested
    class DeleteById {

        @Test
        void shouldDeleteTaskSuccessfully() {
            var id = saveTestTaskRecord("to be deleted");

            dao.deleteById(id);

            var tasks = handle.select("select * from manual_deployment_tasks where id = ?", id)
                .map(new TaskMapper())
                .list();

            assertThat(tasks).isEmpty();
        }

    }

    @Nested
    class FindById {

        @Test
        void shouldFindTaskSuccessfully() {
            var id = saveTestTaskRecord("to be found");

            var task = dao.findById(id).orElseThrow();
            assertThat(task.getSummary()).isEqualTo("to be found");
        }

        @Test
        void shouldReturnOptionalEmptyWhenNotFound() {
            var task = dao.findById(1L);
            assertThat(task).isEmpty();
        }
    }

    @Nested
    class FindByTaskStatusId {

        @Test
        void shouldFindTaskSuccessfully() {
            var id = saveTestTaskRecord("to be found");
            var statusId = saveTestTaskStatusRecord(DeploymentTaskStatus.PENDING, id);

            var task = dao.findByTaskStatusId(statusId).orElseThrow();
            assertThat(task.getId()).isEqualTo(id);
            assertThat(task.getSummary()).isEqualTo("to be found");
        }

        @Test
        void shouldReturnOptionalEmptyWhenNotFound() {
            var task = dao.findByTaskStatusId(1L);
            assertThat(task).isEmpty();
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

}
