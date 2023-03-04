package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.champagne.util.TestObjects.insertDeployableSystem;
import static org.kiwiproject.champagne.util.TestObjects.insertDeploymentEnvironmentRecord;
import static org.kiwiproject.champagne.util.TestObjects.insertTaskRecord;
import static org.kiwiproject.champagne.util.TestObjects.insertTaskStatusRecord;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.util.DateTimeTestHelper.assertTimeDifferenceWithinTolerance;

import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.mappers.TaskStatusMapper;
import org.kiwiproject.champagne.model.manualdeployment.DeploymentTaskStatus;
import org.kiwiproject.champagne.model.manualdeployment.TaskStatus;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@DisplayName("TaskStatusDao")
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
        void shouldInsertTaskStatusSuccessfully() {
            var beforeInsert = ZonedDateTime.now();

            var systemId = insertDeployableSystem(handle, "kiwi");
            var taskId = insertTaskRecord(handle, "Some task");
            var envId = insertDeploymentEnvironmentRecord(handle, "TEST", systemId);

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
            assertThat(status.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance("createdAt", beforeInsert, status.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance("updatedAt", beforeInsert, status.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            assertThat(status.getEnvironmentId()).isEqualTo(envId);
            assertThat(status.getTaskId()).isEqualTo(taskId);
            assertThat(status.getStatus()).isEqualTo(DeploymentTaskStatus.PENDING);
        }
    }

    @Nested
    class FindByTaskId {

        @Test
        void shouldReturnListOfTaskStatuses() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var taskId = insertTaskRecord(handle, "do it");
            insertTaskStatusRecord(handle, DeploymentTaskStatus.PENDING, taskId, systemId);

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
            var systemId = insertDeployableSystem(handle, "kiwi");
            var taskId = insertTaskRecord(handle, "Some Task");
            var statusId = insertTaskStatusRecord(handle, DeploymentTaskStatus.PENDING, taskId, systemId);

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
