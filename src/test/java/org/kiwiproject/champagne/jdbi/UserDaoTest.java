package org.kiwiproject.champagne.jdbi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.jdbc.KiwiJdbc.utcZonedDateTimeFromTimestamp;
import static org.kiwiproject.test.util.DateTimeTestHelper.assertTimeDifferenceWithinTolerance;

import java.sql.Timestamp;
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
import org.kiwiproject.champagne.core.User;
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

        handle.execute("delete from users");
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

            var users = handle.select("select * from users where id = ?", id).mapToMap().list();
            assertThat(users).hasSize(1);

            var user = first(users);
            softly.assertThat(user.get("id")).isEqualTo(id);

            assertTimeDifferenceWithinTolerance(softly, "createdAt", beforeInsert, utcZonedDateTimeFromTimestamp((Timestamp) user.get("created_at")), 1000L);
            assertTimeDifferenceWithinTolerance(softly, "updatedAt", beforeInsert, utcZonedDateTimeFromTimestamp((Timestamp) user.get("updated_at")), 1000L);

            softly.assertThat(user.get("system_identifier")).isEqualTo("doej");
            softly.assertThat(user.get("first_name")).isEqualTo("John");
            softly.assertThat(user.get("last_name")).isEqualTo("Doe");
            softly.assertThat(user.get("display_name")).isEqualTo("John Doe");
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void shouldUpdateUserSuccessfully(SoftAssertions softly) {
            saveTestUserRecord("jdoe", "John", "Doe");
            long userId = handle.select("select * from users where system_identifier = ?", "jdoe")
                .mapToMap()
                .findFirst()
                .map(row -> (long) row.get("id"))
                .orElseThrow();

            var beforeUpdate = ZonedDateTime.now();

            var userToUpdate =  User.builder()
                .id(userId)
                .systemIdentifier("doej")
                .firstName("Foo")
                .lastName("Doe")
                .displayName("Foo Doe")
                .build();
            var id = dao.insertUser(userToUpdate);

            var users = handle.select("select * from users where id = ?", id).mapToMap().list();
            assertThat(users).hasSize(1);

            var user = first(users);
            softly.assertThat(user.get("id")).isEqualTo(id);

            assertTimeDifferenceWithinTolerance(softly, "updatedAt", beforeUpdate, utcZonedDateTimeFromTimestamp((Timestamp) user.get("updated_at")), 1000L);

            softly.assertThat(user.get("system_identifier")).isEqualTo("doej");
            softly.assertThat(user.get("first_name")).isEqualTo("Foo");
            softly.assertThat(user.get("last_name")).isEqualTo("Doe");
            softly.assertThat(user.get("display_name")).isEqualTo("Foo Doe");
        }
    }

    @Nested
    class FindUserBySystemIdentifier {

        @Test
        void shouldReturnOptionalWithUserWhenFound() {
            saveTestUserRecord("doeja", "Jane", "Doe");

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
            saveTestUserRecord("fooBar", "Foo", "Bar");

            var users = dao.findPagedUsers(0, 10);
            assertThat(users)
                .extracting("systemIdentifier", "firstName", "lastName")
                .contains(tuple("fooBar", "Foo", "Bar"));
        }

        @Test
        void shouldReturnEmptyListWhenNoUsersFound() {
            saveTestUserRecord("fooBar", "Foo", "Bar");

            var users = dao.findPagedUsers(10, 10);
            assertThat(users).isEmpty();
        }
    }

    @Nested
    class CountUsers {

        @Test
        void shouldReturnCountOfUsers() {
            saveTestUserRecord("fooBar", "Foo", "Bar");

            var users = dao.countUsers();
            assertThat(users).isOne();
        }

        @Test
        void shouldReturnEmptyListWhenNoUsersFound() {
            var users = dao.countUsers();
            assertThat(users).isZero();
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void shouldDeleteUserSuccessfully(SoftAssertions softly) {
            saveTestUserRecord("jdoe", "John", "Doe");
            long userId = handle.select("select * from users where system_identifier = ?", "jdoe")
                .mapToMap()
                .findFirst()
                .map(row -> (long) row.get("id"))
                .orElseThrow();

            dao.deleteUser(userId);

            var users = handle.select("select * from users where id = ?", userId).mapToMap().list();
            assertThat(users).isEmpty();
        }

    }

    private void saveTestUserRecord(String systemIdentifier, String firstName, String lastName) {
        handle.execute("insert into users (first_name, last_name, display_name, system_identifier) values (?, ?, ?, ?)", firstName, lastName, firstName + " " + lastName, systemIdentifier);
    }
}
