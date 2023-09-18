package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.champagne.util.TestObjects.insertDeployableSystem;
import static org.kiwiproject.champagne.util.TestObjects.insertDeploymentEnvironmentRecord;
import static org.kiwiproject.champagne.util.TestObjects.insertDeploymentExecution;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.util.DateTimeTestHelper.assertTimeDifferenceWithinTolerance;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.mappers.DeploymentExecutionMapper;
import org.kiwiproject.champagne.model.DeploymentExecution;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

@DisplayName("DeploymentExecutionDao")
class DeploymentExecutionDaoTest {
    
    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<DeploymentExecutionDao> daoExtension = Jdbi3DaoExtension.<DeploymentExecutionDao>builder()
            .daoType(DeploymentExecutionDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private DeploymentExecutionDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertDeploymentExecution {

        @Test
        void shouldInsertDeploymentExecutionSuccessfully() {
            var beforeInsert = ZonedDateTime.now();

            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);

            var executionToInsert = DeploymentExecution.builder()
                    .status(DeploymentExecution.Status.RUNNING)
                    .environmentId(envId)
                    .deployableSystemId(systemId)
                    .build();

            var id = dao.insertDeploymentExecution(executionToInsert);

            var deploymentExecutions = handle.select("select * from deployment_executions where id = ?", id)
                .map(new DeploymentExecutionMapper())
                .list();

            assertThat(deploymentExecutions).hasSize(1);

            var deploymentExecution = first(deploymentExecutions);
            assertThat(deploymentExecution.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance("createdAt", beforeInsert, deploymentExecution.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance("updatedAt", beforeInsert, deploymentExecution.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            assertThat(deploymentExecution).usingRecursiveComparison().ignoringFields("id", "createdAt", "updatedAt").isEqualTo(executionToInsert);
        }
    }

    @Nested
    class FindPagedDeploymentExecution {

        @Test
        void shouldReturnListOfDeploymentExecution() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            insertDeploymentExecution(handle, envId, systemId, DeploymentExecution.Status.RUNNING);

            var executions = dao.findPageOfDeploymentExecutions(systemId, 0, 10);
            assertThat(executions).hasSize(1);
        }

        @Test
        void shouldReturnEmptyListWhenNoDeploymentExecutionsFound() {
            var systemId = insertDeployableSystem(handle, "kiwi");

            var executions = dao.findPageOfDeploymentExecutions(systemId, 10, 10);
            assertThat(executions).isEmpty();
        }
    }

    @Nested
    class CountDeploymentExecutions {

        @Test
        void shouldReturnCountOfDeploymentExecutions() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            insertDeploymentExecution(handle, envId, systemId, DeploymentExecution.Status.RUNNING);

            var executions = dao.countDeploymentExecutions(systemId);
            assertThat(executions).isOne();
        }

        @Test
        void shouldReturnEmptyListWhenNoExecutionsFound() {
            var executions = dao.countDeploymentExecutions(1L);
            assertThat(executions).isZero();
        }
    }
   
}
