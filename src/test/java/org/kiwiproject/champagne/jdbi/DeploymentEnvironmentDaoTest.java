package org.kiwiproject.champagne.jdbi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.util.DateTimeTestHelper.assertTimeDifferenceWithinTolerance;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.core.DeploymentEnvironment;
import org.kiwiproject.champagne.jdbi.mappers.DeploymentEnvironmentMapper;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@DisplayName("DeploymentEnvironmentDao")
@ExtendWith(SoftAssertionsExtension.class)
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

    private long testUserId;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();

        testUserId = saveTestUserRecord("jdoe");
    }

    @Nested
    class InsertEnvironment {

        @Test
        void shouldInsertUserSuccessfully(SoftAssertions softly) {
            var beforeInsert = ZonedDateTime.now();

            var envToInsert = DeploymentEnvironment.builder()
                    .name("PRODUCTION")
                    .createdById(testUserId)
                    .updatedById(testUserId)
                    .build();

            var id = dao.insertEnvironment(envToInsert);

            var envs = handle.select("select * from deployment_environments where id = ?", id)
                .map(new DeploymentEnvironmentMapper())
                .list();

            assertThat(envs).hasSize(1);

            var env = first(envs);
            softly.assertThat(env.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance(softly, "createdAt", beforeInsert, env.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance(softly, "updatedAt", beforeInsert, env.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            softly.assertThat(env.getName()).isEqualTo("PRODUCTION");
            softly.assertThat(env.getCreatedById()).isEqualTo(testUserId);
            softly.assertThat(env.getUpdatedById()).isEqualTo(testUserId);
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void shouldUpdateDeploymentEnvironmentSuccessfully(SoftAssertions softly) {
            long envId = saveTestDeploymentEnvironmentRecord("TEST");
            long testSecondUser = saveTestUserRecord("jadoe");

            var envToUpdate = DeploymentEnvironment.builder()
                    .id(envId)
                    .name("PRODUCTION")
                    .updatedById(testSecondUser)
                    .build();

            dao.updateEnvironment(envToUpdate);

            var users = handle.select("select * from deployment_environments where id = ?", envId)
                .map(new DeploymentEnvironmentMapper())
                .list();

            assertThat(users).hasSize(1);

            var user = first(users);
            softly.assertThat(user.getName()).isEqualTo("PRODUCTION");
            softly.assertThat(user.getCreatedById()).isEqualTo(1L);
            softly.assertThat(user.getUpdatedById()).isEqualTo(2L);
        }
    }

    @Nested
    class FindAllEnvironments {

        @Test
        void shouldReturnListOfDeploymentEnvironments() {
            saveTestDeploymentEnvironmentRecord("DEV");

            var environments = dao.findAllEnvironments();
            assertThat(environments)
                .extracting("name")
                .contains("DEV");
        }

        @Test
        void shouldReturnEmptyListWhenNoDeploymentEnvironmentsFound() {
            var environments = dao.findAllEnvironments();
            assertThat(environments).isEmpty();
        }
    }

    @Nested
    class HardDeleteById {

        @Test
        void shouldDeleteDeploymentEnvironmentSuccessfully() {
            long envId = saveTestDeploymentEnvironmentRecord("TRAINING");

            dao.hardDeleteById(envId);

            var envs = handle.select("select * from deployment_environments where id = ?", envId).mapToMap().list();
            assertThat(envs).isEmpty();
        }

    }

    @Nested
    class SoftDeleteById {

        @Test
        void shouldSoftDeleteDeploymentEnvironmentSuccessfully(SoftAssertions softly) {
            var id = saveTestDeploymentEnvironmentRecord("TEST");

            dao.softDeleteById(id, testUserId);

            var envs = handle.select("select * from deployment_environments where id = ?", id)
                .map(new DeploymentEnvironmentMapper())
                .list();

            assertThat(envs).hasSize(1);

            var user = first(envs);
            softly.assertThat(user.getId()).isEqualTo(id);
            softly.assertThat(user.isDeleted()).isEqualTo(true);
        }

    }

    @Nested
    class UnDeleteById {

        @Test
        void shouldUnDeleteDeploymentEnvironmentSuccessfully(SoftAssertions softly) {
            var id = saveTestDeploymentEnvironmentRecord("TEST");
            handle.execute("update deployment_environments set deleted=true where id = ?", id);

            dao.unSoftDeleteById(id, testUserId);

            var envs = handle.select("select * from deployment_environments where id = ?", id)
                .map(new DeploymentEnvironmentMapper())
                .list();

            assertThat(envs).hasSize(1);

            var user = first(envs);
            softly.assertThat(user.getId()).isEqualTo(id);
            softly.assertThat(user.isDeleted()).isEqualTo(false);
        }

    }

    private long saveTestDeploymentEnvironmentRecord(String name) {
        handle.execute("insert into deployment_environments (environment_name, created_by, updated_by) values (?, ?, ?)", name, testUserId, testUserId);

        return handle.select("select * from deployment_environments where environment_name = ?", name)
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
