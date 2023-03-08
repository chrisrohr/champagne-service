package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.champagne.util.TestObjects.insertDeployableSystem;
import static org.kiwiproject.champagne.util.TestObjects.insertDeploymentEnvironmentRecord;
import static org.kiwiproject.champagne.util.TestObjects.insertUserRecord;
import static org.kiwiproject.champagne.util.TestObjects.insertUserToDeployableSystemLink;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.util.DateTimeTestHelper.assertTimeDifferenceWithinTolerance;

import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.mappers.DeployableSystemMapper;
import org.kiwiproject.champagne.model.DeployableSystem;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@DisplayName("DeployableSystemDao")
class DeployableSystemDaoTest {
    
    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<DeployableSystemDao> daoExtension = Jdbi3DaoExtension.<DeployableSystemDao>builder()
            .daoType(DeployableSystemDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private DeployableSystemDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertDeployableSystem {

        @Test
        void shouldInsertSuccessfully() {
            var beforeInsert = ZonedDateTime.now();

            var deployableSystemToInsert = DeployableSystem.builder().name("kiwi").build();

            var id = dao.insertDeployableSystem(deployableSystemToInsert);

            var deployableSystems = handle.select("select * from deployable_systems where id = ?", id)
                .map(new DeployableSystemMapper())
                .list();

            assertThat(deployableSystems).hasSize(1);

            var deployableSystem = first(deployableSystems);
            assertThat(deployableSystem.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance("createdAt", beforeInsert, deployableSystem.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance("updatedAt", beforeInsert, deployableSystem.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            assertThat(deployableSystem).usingRecursiveComparison().ignoringFields("id", "createdAt", "updatedAt").isEqualTo(deployableSystemToInsert);
        }
    }

    @Nested
    class FindPagedDeployableSystems {

        @Test
        void shouldReturnListOfDeployableSystems() {
            insertDeployableSystem(handle, "kiwi");

            var deployableSystems = dao.findPagedDeployableSystems(0, 10);
            assertThat(deployableSystems)
                .extracting("name")
                .contains("kiwi");
        }

        @Test
        void shouldReturnEmptyListWhenNoDeployableSystemsFound() {
            insertDeployableSystem(handle, "kiwi");

            var deployableSystems = dao.findPagedDeployableSystems(10, 10);
            assertThat(deployableSystems).isEmpty();
        }
    }

    @Nested
    class CountDeployableSystems {

        @Test
        void shouldReturnCountOfDeployableSystems() {
            insertDeployableSystem(handle, "kiwi");

            var count = dao.countDeployableSystems();
            assertThat(count).isOne();
        }

        @Test
        void shouldReturnEmptyListWhenNoDeployableSystemsFound() {
            var count = dao.countDeployableSystems();
            assertThat(count).isZero();
        }
    }

    @Nested
    class FindDeployableSystemsForUser {

        @Test
        void shouldReturnDeployableSystemsForGivenUserPopulatedWithAdminFlag() {
            var userId = insertUserRecord(handle, "jdoe");
            var deployableSystem1 = insertDeployableSystem(handle, "system1");
            var deployableSystem2 = insertDeployableSystem(handle, "system2");
            insertUserToDeployableSystemLink(handle, userId, deployableSystem1, false);
            insertUserToDeployableSystemLink(handle, userId, deployableSystem2, true);

            var userToNotFind = insertUserRecord(handle, "bob");
            var deployableSystemToNotFind = insertDeployableSystem(handle, "system3");
            insertUserToDeployableSystemLink(handle, userToNotFind, deployableSystemToNotFind, false);

            var deployableSystems = dao.findDeployableSystemsForUser(userId);

            assertThat(deployableSystems)
                    .extracting("name", "admin")
                    .contains(tuple("system1", false), tuple("system2", true));
        }
    }

    @Nested
    class UpdateDevEnvironment {

        @Test
        void shouldUpdateTheDevEnvOfAGivenRecord() {
            var systemId = insertDeployableSystem(handle, "kiwi");
            var envId = insertDeploymentEnvironmentRecord(handle, "dev", systemId);
            var deployableSystemId = insertDeployableSystem(handle, "kiwi");

            dao.updateDevEnvironment(deployableSystemId, envId);

            var deployableSystems = handle.select("select * from deployable_systems where id = ?", deployableSystemId)
                    .map(new DeployableSystemMapper())
                    .list();

            assertThat(deployableSystems).hasSize(1);

            var status = first(deployableSystems);
            assertThat(status.getDevEnvironmentId()).isEqualTo(envId);
        }
    }

    @Nested
    class UpdateEnvironmentPromotionOrder {

        @Test
        void shouldUpdateThePromotionOrderOfAGivenRecord() {
            var deployableSystemId = insertDeployableSystem(handle, "kiwi");

            dao.updateEnvironmentPromotionOrder(deployableSystemId, "1,3,4,2");

            var deployableSystems = handle.select("select * from deployable_systems where id = ?", deployableSystemId)
                    .map(new DeployableSystemMapper())
                    .list();

            assertThat(deployableSystems).hasSize(1);

            var status = first(deployableSystems);
            assertThat(status.getEnvironmentPromotionOrder()).isEqualTo("1,3,4,2");
        }
    }

    @Nested
    class DeleteById {

        @Test
        void shouldDeleteTaskSuccessfully() {
            var id = insertDeployableSystem(handle, "to be deleted");

            dao.deleteById(id);

            var deployableSystems = handle.select("select * from deployable_systems where id = ?", id)
                    .map(new DeployableSystemMapper())
                    .list();

            assertThat(deployableSystems).isEmpty();
        }

    }

    @Nested
    class IsUserAdminOfSystem {

        @Test
        void shouldReturnTrueWhenUserIsAnAdminForSystem() {
            var userId = insertUserRecord(handle, "jdoe");
            var deployableSystemId = insertDeployableSystem(handle, "system1");
            insertUserToDeployableSystemLink(handle, userId, deployableSystemId, true);

            var result = dao.isUserAdminOfSystem(userId, deployableSystemId);

            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalseWhenUserIsNotAnAdminForSystem() {
            var userId = insertUserRecord(handle, "jdoe");
            var deployableSystemId = insertDeployableSystem(handle, "system1");
            insertUserToDeployableSystemLink(handle, userId, deployableSystemId, false);

            var result = dao.isUserAdminOfSystem(userId, deployableSystemId);

            assertThat(result).isFalse();
        }
    }

    @Nested
    class FindUsersForSystem {

        @Test
        void shouldReturnListOfUsersForASystem() {
            var userId = insertUserRecord(handle, "jdoe");
            var deployableSystemId = insertDeployableSystem(handle, "system1");
            insertUserToDeployableSystemLink(handle, userId, deployableSystemId, true);

            var users = dao.findUsersForSystem(deployableSystemId);

            assertThat(users).extracting("userId", "admin").contains(tuple(userId, true));
        }
    }

    @Nested
    class IsUserInSystem {

        @Test
        void shouldReturnTrueWhenUserIsAssignedToSystem() {
            var userId = insertUserRecord(handle, "jdoe");
            var deployableSystemId = insertDeployableSystem(handle, "system1");
            insertUserToDeployableSystemLink(handle, userId, deployableSystemId, true);

            var result = dao.isUserInSystem(userId, deployableSystemId);

            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnNullWhenUserIsNotAssignedToSystem() {
            var userId = insertUserRecord(handle, "jdoe");
            var deployableSystemId = insertDeployableSystem(handle, "system1");
            insertUserToDeployableSystemLink(handle, userId, deployableSystemId, true);

            var result = dao.isUserInSystem(userId+1, deployableSystemId);

            assertThat(result).isNull();
        }
    }

    @Nested
    class InsertOrUpdateSystemUser {

        @Test
        void shouldInsertNewUserSystemLinkWhenDoesNotExist() {
            var userId = insertUserRecord(handle, "jdoe");
            var deployableSystemId = insertDeployableSystem(handle, "system1");

            assertThat(dao.isUserInSystem(userId, deployableSystemId)).isNull();

            dao.insertOrUpdateSystemUser(deployableSystemId, userId, false);

            assertThat(dao.isUserInSystem(userId, deployableSystemId)).isTrue();
        }

        @Test
        void shouldUpdateUserSystemLinkWhenExists() {
            var userId = insertUserRecord(handle, "jdoe");
            var deployableSystemId = insertDeployableSystem(handle, "system1");
            insertUserToDeployableSystemLink(handle, userId, deployableSystemId, true);

            var systemUsers = dao.findUsersForSystem(deployableSystemId);
            assertThat(systemUsers).extracting("userId", "admin").contains(tuple(userId, true));

            dao.insertOrUpdateSystemUser(deployableSystemId, userId, false);

            systemUsers = dao.findUsersForSystem(deployableSystemId);
            assertThat(systemUsers).extracting("userId", "admin").contains(tuple(userId, false));
        }
    }
   
}
