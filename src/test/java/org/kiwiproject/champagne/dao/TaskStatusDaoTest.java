package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.champagne.dao.TestDbObjects.saveTestDeploymentEnvironmentRecord;
import static org.kiwiproject.champagne.dao.TestDbObjects.saveTestTaskRecord;
import static org.kiwiproject.champagne.dao.TestDbObjects.saveTestTaskStatusRecord;
import static org.kiwiproject.champagne.dao.TestDbObjects.saveTestUserRecord;
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

            var taskId = saveTestTaskRecord(handle, "Some task");
            var userId = saveTestUserRecord(handle, "jdoe");
            var envId = saveTestDeploymentEnvironmentRecord(handle, "TEST", userId);

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
            var taskId = saveTestTaskRecord(handle, "do it");
            saveTestTaskStatusRecord(handle, DeploymentTaskStatus.PENDING, taskId);

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
            var taskId = saveTestTaskRecord(handle, "Some Task");
            var statusId = saveTestTaskStatusRecord(handle, DeploymentTaskStatus.PENDING, taskId);

            dao.updateStatus(statusId, DeploymentTaskStatus.COMPLETE);

            var taskStatuses = handle.select("select * from manual_deployment_task_statuses where id = ?", statusId)
                .map(new TaskStatusMapper())
                .list();

            assertThat(taskStatuses).hasSize(1);

            var status = first(taskStatuses);
            assertThat(status.getStatus()).isEqualTo(DeploymentTaskStatus.COMPLETE);
        }
    }

}
