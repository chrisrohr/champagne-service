package org.kiwiproject.champagne.jdbi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.jdbc.KiwiJdbc.utcZonedDateTimeFromTimestamp;
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
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

import java.sql.Timestamp;
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

    @SuppressWarnings("SqlWithoutWhere")
    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();

        handle.execute("delete from deployment_environments");
        handle.execute("delete from users");

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

            var envs = handle.select("select * from deployment_environments where id = ?", id).mapToMap().list();
            assertThat(envs).hasSize(1);

            var env = first(envs);
            softly.assertThat(env.get("id")).isEqualTo(id);

            assertTimeDifferenceWithinTolerance(softly, "createdAt", beforeInsert, utcZonedDateTimeFromTimestamp((Timestamp) env.get("created_at")), 1000L);
            assertTimeDifferenceWithinTolerance(softly, "updatedAt", beforeInsert, utcZonedDateTimeFromTimestamp((Timestamp) env.get("updated_at")), 1000L);

            softly.assertThat(env.get("environment_name")).isEqualTo("PRODUCTION");
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

            var users = handle.select("select * from deployment_environments where id = ?", envId).mapToMap().list();
            assertThat(users).hasSize(1);

            var user = first(users);
            softly.assertThat(user.get("environment_name")).isEqualTo("PRODUCTION");
            softly.assertThat(user.get("created_by")).isEqualTo(1L);
            softly.assertThat(user.get("updated_by")).isEqualTo(2L);
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
    class DeleteById {

        @Test
        void shouldDeleteDeploymentEnvironmentSuccessfully() {
            long envId = saveTestDeploymentEnvironmentRecord("TRAINING");

            dao.deleteById(envId);

            var envs = handle.select("select * from deployment_environments where id = ?", envId).mapToMap().list();
            assertThat(envs).isEmpty();
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
