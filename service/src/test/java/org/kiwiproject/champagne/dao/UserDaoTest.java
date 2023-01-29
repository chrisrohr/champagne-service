package org.kiwiproject.champagne.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.champagne.util.TestObjects.insertUserRecord;
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
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.champagne.dao.mappers.UserMapper;
import org.kiwiproject.test.junit.jupiter.Jdbi3DaoExtension;
import org.kiwiproject.test.junit.jupiter.PostgresLiquibaseTestExtension;

@DisplayName("UserDao")
@ExtendWith(SoftAssertionsExtension.class)
class UserDaoTest {
    
    @RegisterExtension
    static final PostgresLiquibaseTestExtension POSTGRES = new PostgresLiquibaseTestExtension("migrations.xml");

    @RegisterExtension
    final Jdbi3DaoExtension<UserDao> daoExtension = Jdbi3DaoExtension.<UserDao>builder()
            .daoType(UserDao.class)
            .dataSource(POSTGRES.getTestDataSource())
            .build();

    private UserDao dao;
    private Handle handle;

    @BeforeEach
    void setUp() {
        dao = daoExtension.getDao();
        handle = daoExtension.getHandle();
    }

    @Nested
    class InsertUser {

        @Test
        void shouldInsertUserSuccessfully(SoftAssertions softly) {
            var beforeInsert = ZonedDateTime.now();

            var userToInsert = User.builder()
                .systemIdentifier("doej")
                .firstName("John")
                .lastName("Doe")
                .displayName("John Doe")
                .build();

            var id = dao.insertUser(userToInsert);

            var users = handle.select("select * from users where id = ?", id)
                .map(new UserMapper())
                .list();

            assertThat(users).hasSize(1);

            var user = first(users);
            softly.assertThat(user.getId()).isEqualTo(id);

            assertTimeDifferenceWithinTolerance(softly, "createdAt", beforeInsert, user.getCreatedAt().atZone(ZoneOffset.UTC), 1000L);
            assertTimeDifferenceWithinTolerance(softly, "updatedAt", beforeInsert, user.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            softly.assertThat(user.getSystemIdentifier()).isEqualTo("doej");
            softly.assertThat(user.getFirstName()).isEqualTo("John");
            softly.assertThat(user.getLastName()).isEqualTo("Doe");
            softly.assertThat(user.getDisplayName()).isEqualTo("John Doe");
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void shouldUpdateUserSuccessfully(SoftAssertions softly) {
            var userId = insertUserRecord(handle, "jdoe");

            var beforeUpdate = ZonedDateTime.now();

            var userToUpdate =  User.builder()
                .id(userId)
                .systemIdentifier("doej")
                .firstName("Foo")
                .lastName("Doe")
                .displayName("Foo Doe")
                .build();

            dao.updateUser(userToUpdate);

            var users = handle.select("select * from users where id = ?", userId)
                .map(new UserMapper())
                .list();

            assertThat(users).hasSize(1);

            var user = first(users);
            softly.assertThat(user.getId()).isEqualTo(userId);

            assertTimeDifferenceWithinTolerance(softly, "updatedAt", beforeUpdate, user.getUpdatedAt().atZone(ZoneOffset.UTC), 1000L);

            softly.assertThat(user.getSystemIdentifier()).isEqualTo("doej");
            softly.assertThat(user.getFirstName()).isEqualTo("Foo");
            softly.assertThat(user.getLastName()).isEqualTo("Doe");
            softly.assertThat(user.getDisplayName()).isEqualTo("Foo Doe");
        }
    }

    @Nested
    class FindUserBySystemIdentifier {

        @Test
        void shouldReturnOptionalWithUserWhenFound() {
            insertUserRecord(handle, "doeja", "Jane", "Doe");

            var user = dao.findBySystemIdentifier("doeja");
            assertThat(user).isPresent();
        }

        @Test
        void shouldReturnEmptyOptionalWhenUserNotFound() {
            var user = dao.findBySystemIdentifier("doeja");
            assertThat(user).isEmpty();
        }
    }

    @Nested
    class FindPagedUsers {

        @Test
        void shouldReturnListOfUsers() {
            insertUserRecord(handle, "fooBar", "Foo", "Bar");

            var users = dao.findPagedUsers(0, 10);
            assertThat(users)
                .extracting("systemIdentifier", "firstName", "lastName")
                .contains(tuple("fooBar", "Foo", "Bar"));
        }

        @Test
        void shouldReturnEmptyListWhenNoUsersFound() {
            insertUserRecord(handle, "fooBar", "Foo", "Bar");

            var users = dao.findPagedUsers(10, 10);
            assertThat(users).isEmpty();
        }
    }

    @Nested
    class CountUsers {

        @Test
        void shouldReturnCountOfUsers() {
            insertUserRecord(handle, "fooBar", "Foo", "Bar");

            var count = dao.countUsers();
            assertThat(count).isOne();
        }

        @Test
        void shouldReturnZeroWhenNoUsersFound() {
            var count = dao.countUsers();
            assertThat(count).isZero();
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void shouldDeleteUserSuccessfully(SoftAssertions softly) {
            var userId = insertUserRecord(handle, "jdoe");

            dao.deleteUser(userId);

            var users = handle.select("select * from users where id = ?", userId).map(new UserMapper()).list();
            assertThat(users).isEmpty();
        }

    }

}
