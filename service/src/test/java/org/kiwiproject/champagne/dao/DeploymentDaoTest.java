package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.champagne.util.TestObjects.insertBuildRecord;
import static org.kiwiproject.champagne.util.TestObjects.insertDeployableSystem;
import static org.kiwiproject.champagne.util.TestObjects.insertDeployment;
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
import org.kiwiproject.champagne.dao.mappers.DeploymentMapper;
import org.kiwiproject.champagne.model.Deployment;
import org.kiwiproject.champagne.model.DeploymentExecution;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

@DisplayName("DeploymentDao")
class DeploymentDaoTest {
    
    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<DeploymentDao> daoExtension = Jdbi3DaoExtension.<DeploymentDao>builder()
            .daoType(DeploymentDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private DeploymentDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertDeployment {

        @Test
        void shouldInsertDeploymentSuccessfully() {
            var beforeInsert = ZonedDateTime.now();

            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var buildId = insertBuildRecord(handle, "some-service", "42.0.0", systemId);
            var executionId = insertDeploymentExecution(handle, systemId, envId, DeploymentExecution.Status.RUNNING);

            var deploymentToInsert = Deployment.builder()
                    .status(Deployment.Status.RUNNING)
                    .buildId(buildId)
                    .environmentId(envId)
                    .executionId(executionId)
                    .deployableSystemId(systemId)
                    .build();

            var id = dao.insertDeployment(deploymentToInsert);

            var deployments = handle.select("select * from deployments where id = ?", id)
                .map(new DeploymentMapper())
                .list();

            assertThat(deployments).hasSize(1);

            var deployment = first(deployments);
            assertThat(deployment.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance("createdAt", beforeInsert, deployment.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance("updatedAt", beforeInsert, deployment.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            assertThat(deployment).usingRecursiveComparison().ignoringFields("id", "createdAt", "updatedAt").isEqualTo(deploymentToInsert);
        }
    }

    @Nested
    class FindPagedDeployments {

        @Test
        void shouldReturnListOfDeployments() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var buildId = insertBuildRecord(handle, "some-service", "42.0.0", systemId);
            var executionId = insertDeploymentExecution(handle, envId, systemId, DeploymentExecution.Status.RUNNING);
            insertDeployment(handle, systemId, envId, executionId, buildId, Deployment.Status.STAGED);

            var deployments = dao.findPageOfDeploymentsInExecution(executionId, 0, 10);
            assertThat(deployments).hasSize(1);
        }

        @Test
        void shouldReturnEmptyListWhenNoDeploymentsFound() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var executionId = insertDeploymentExecution(handle, envId, systemId, DeploymentExecution.Status.RUNNING);

            var deployments = dao.findPageOfDeploymentsInExecution(executionId, 10, 10);
            assertThat(deployments).isEmpty();
        }
    }

    @Nested
    class CountDeployments {

        @Test
        void shouldReturnCountOfDeployments() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var buildId = insertBuildRecord(handle, "some-service", "42.0.0", systemId);
            var executionId = insertDeploymentExecution(handle, envId, systemId, DeploymentExecution.Status.RUNNING);
            insertDeployment(handle, systemId, envId, executionId, buildId, Deployment.Status.STAGED);

            var count = dao.countDeploymentsInExecution(executionId);
            assertThat(count).isOne();
        }

        @Test
        void shouldReturnEmptyListWhenNoDeploymentsFound() {
            var executions = dao.countDeploymentsInExecution(1L);
            assertThat(executions).isZero();
        }
    }
   
}
