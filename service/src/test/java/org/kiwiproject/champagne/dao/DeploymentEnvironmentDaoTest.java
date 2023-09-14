package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.champagne.util.TestObjects.insertDeployableSystem;
import static org.kiwiproject.champagne.util.TestObjects.insertDeploymentEnvironmentRecord;
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
import org.kiwiproject.champagne.dao.mappers.DeploymentEnvironmentMapper;
import org.kiwiproject.champagne.model.DeploymentEnvironment;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

@DisplayName("DeploymentEnvironmentDao")
class DeploymentEnvironmentDaoTest {
    
    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<DeploymentEnvironmentDao> daoExtension = Jdbi3DaoExtension.<DeploymentEnvironmentDao>builder()
            .daoType(DeploymentEnvironmentDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private DeploymentEnvironmentDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertEnvironment {

        @Test
        void shouldInsertUserSuccessfully() {
            var beforeInsert = ZonedDateTime.now();

            var envToInsert = DeploymentEnvironment.builder()
                    .name("PRODUCTION")
                    .build();

            var id = dao.insertEnvironment(envToInsert);

            var envs = handle.select("select * from deployment_environments where id = ?", id)
                .map(new DeploymentEnvironmentMapper())
                .list();

            assertThat(envs).hasSize(1);

            var env = first(envs);
            assertThat(env.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance("createdAt", beforeInsert, env.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance("updatedAt", beforeInsert, env.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            assertThat(env.getName()).isEqualTo("PRODUCTION");
        }
    }

    @Nested
    class UpdateEnv {

        @Test
        void shouldUpdateDeploymentEnvironmentSuccessfully() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            long envId = insertDeploymentEnvironmentRecord(handle, "TEST", systemId);

            var envToUpdate = DeploymentEnvironment.builder()
                    .id(envId)
                    .name("PRODUCTION")
                    .build();

            dao.updateEnvironment(envToUpdate);

            var envs = handle.select("select * from deployment_environments where id = ?", envId)
                .map(new DeploymentEnvironmentMapper())
                .list();

            assertThat(envs).hasSize(1);

            var user = first(envs);
            assertThat(user.getName()).isEqualTo("PRODUCTION");
        }
    }

    @Nested
    class FindAllEnvironments {

        @Test
        void shouldReturnListOfDeploymentEnvironments() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            insertDeploymentEnvironmentRecord(handle, "DEV", systemId);

            var environments = dao.findAllEnvironments(systemId);
            assertThat(environments)
                .extracting("name")
                .contains("DEV");
        }

        @Test
        void shouldReturnEmptyListWhenNoDeploymentEnvironmentsFound() {
            var environments = dao.findAllEnvironments(1L);
            assertThat(environments).isEmpty();
        }
    }

    @Nested
    class GetEnvironmentName {

        @Test
        void shouldReturnNameOfGivenEnvironment() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "DEV", systemId);

            var name = dao.getEnvironmentName(envId);
            assertThat(name).isEqualTo("DEV");
        }
    }

    @Nested
    class HardDeleteById {

        @Test
        void shouldDeleteDeploymentEnvironmentSuccessfully() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            long envId = insertDeploymentEnvironmentRecord(handle, "TRAINING", systemId);

            dao.hardDeleteById(envId);

            var envs = handle.select("select * from deployment_environments where id = ?", envId).mapToMap().list();
            assertThat(envs).isEmpty();
        }

    }

    @Nested
    class SoftDeleteById {

        @Test
        void shouldSoftDeleteDeploymentEnvironmentSuccessfully() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var id = insertDeploymentEnvironmentRecord(handle, "TEST", systemId);

            dao.softDeleteById(id);

            var envs = handle.select("select * from deployment_environments where id = ?", id)
                .map(new DeploymentEnvironmentMapper())
                .list();

            assertThat(envs).hasSize(1);

            var user = first(envs);
            assertThat(user.getId()).isEqualTo(id);
            assertThat(user.isDeleted()).isTrue();
        }

    }

    @Nested
    class UnDeleteById {

        @Test
        void shouldUnDeleteDeploymentEnvironmentSuccessfully() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var id = insertDeploymentEnvironmentRecord(handle, "TEST", systemId);
            handle.execute("update deployment_environments set deleted=true where id = ?", id);

            dao.unSoftDeleteById(id);

            var envs = handle.select("select * from deployment_environments where id = ?", id)
                .map(new DeploymentEnvironmentMapper())
                .list();

            assertThat(envs).hasSize(1);

            var user = first(envs);
            assertThat(user.getId()).isEqualTo(id);
            assertThat(user.isDeleted()).isFalse();
        }

    }

}
