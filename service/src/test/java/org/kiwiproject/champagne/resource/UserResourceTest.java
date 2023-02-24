package org.kiwiproject.champagne.resource;

import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertNoContentResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertAcceptedResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertNotFoundResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertResponseStatusCode;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertInternalServerErrorResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.config.AppConfig;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.champagne.junit.jupiter.JwtExtension;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.champagne.resource.apps.TestUserApp;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;
import org.kiwiproject.dropwizard.error.test.junit.jupiter.ApplicationErrorExtension;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;
import org.kiwiproject.spring.data.KiwiPage;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.GenericType;

@DisplayName("UserResource")
@ExtendWith({ DropwizardExtensionsSupport.class, SoftAssertionsExtension.class, ApplicationErrorExtension.class })
public class UserResourceTest {

    private static final UserDao USER_DAO = mock(UserDao.class);
    private static final AuditRecordDao AUDIT_RECORD_DAO = mock(AuditRecordDao.class);
    private static final ApplicationErrorDao APPLICATION_ERROR_DAO = mock(ApplicationErrorDao.class);
    private static final UserResource USER_RESOURCE = new UserResource(USER_DAO, AUDIT_RECORD_DAO, APPLICATION_ERROR_DAO);

    private static final ResourceExtension APP = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(USER_RESOURCE)
            .addProvider(JaxrsExceptionMapper.class)
            .build();

    @RegisterExtension
    private final JwtExtension jwtExtension = new JwtExtension("bob");
    
    @BeforeEach
    void setUp() {
        reset(USER_DAO, AUDIT_RECORD_DAO);
    }

    @Nested
    class ListUsers {

        @Test
        void shouldReturnPagedListOfUsersUsingDefaultPagingParams() {
            var user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .displayName("John Doe")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

            when(USER_DAO.findPagedUsers(0, 25)).thenReturn(List.of(user));
            when(USER_DAO.countUsers()).thenReturn(1L);

            var response = APP.client().target("/users")
                .request()
                .get();

            assertOkResponse(response);

            var pagedData = response.readEntity(new GenericType<KiwiPage<User>>(){});
            assertThat(pagedData.getNumberOfElements()).isOne();
            assertThat(pagedData.getTotalElements()).isOne();
            assertThat(pagedData.getNumber()).isOne();

            var foundUser = first(pagedData.getContent());
            assertThat(foundUser)
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt")
                .isEqualTo(user);

            verify(USER_DAO).findPagedUsers(0, 25);
            verify(USER_DAO).countUsers();
            verifyNoMoreInteractions(USER_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }

        @Test
        void shouldReturnPagedListOfUsersUsingProvidedPagingParams() {
            var user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .displayName("John Doe")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

            when(USER_DAO.findPagedUsers(40, 10)).thenReturn(List.of(user));
            when(USER_DAO.countUsers()).thenReturn(41L);

            var response = APP.client().target("/users")
                .queryParam("pageNumber", 5)
                .queryParam("pageSize", 10)
                .request()
                .get();

            assertOkResponse(response);

            var pagedData = response.readEntity(new GenericType<KiwiPage<User>>(){});
            assertThat(pagedData.getNumberOfElements()).isOne();
            assertThat(pagedData.getTotalElements()).isEqualTo(41L);
            assertThat(pagedData.getNumber()).isEqualTo(5L);

            var foundUser = first(pagedData.getContent());
            assertThat(foundUser)
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt")
                .isEqualTo(user);

            verify(USER_DAO).findPagedUsers(40, 10);
            verify(USER_DAO).countUsers();
            verifyNoMoreInteractions(USER_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }
    }

    @Nested
    class AddUser {

        @Test
        void shouldAddTheGivenUser() {
            when(USER_DAO.insertUser(any(User.class))).thenReturn(1L);

            var user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .displayName("John Doe")
                .systemIdentifier("jdoe")
                .build();

            var response = APP.client().target("/users")
                .request()
                .post(json(user));

            assertNoContentResponse(response);

            verify(USER_DAO).insertUser(isA(User.class));

            verifyAuditRecorded(1L, Action.CREATED);

            verifyNoMoreInteractions(USER_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAddTheGivenUserWhenValidationErrors() {
            var user = User.builder().build();

            var response = APP.client().target("/users")
                .request()
                .post(json(user));

            assertResponseStatusCode(response, 422);

            verifyNoInteractions(USER_DAO, AUDIT_RECORD_DAO);
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void shouldDeleteGivenUser() {
            when(USER_DAO.deleteUser(1L)).thenReturn(1);

            var response = APP.client().target("/users")
                .path("{id}")
                .resolveTemplate("id", 1)
                .request()
                .delete();

            assertNoContentResponse(response);

            verify(USER_DAO).deleteUser(1L);
            
            verifyAuditRecorded(1L, Action.DELETED);

            verifyNoMoreInteractions(USER_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAuditIfDeleteDoesNotChangeDB() {
            when(USER_DAO.deleteUser(1L)).thenReturn(0);
            
            var response = APP.client().target("/users")
                .path("{id}")
                .resolveTemplate("id", 1)
                .request()
                .delete();

            assertNoContentResponse(response);

            verify(USER_DAO).deleteUser(1L);
            verifyNoMoreInteractions(USER_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }
    }

    private void verifyAuditRecorded(long id, Action action) {
        var argCapture = ArgumentCaptor.forClass(AuditRecord.class);
        verify(AUDIT_RECORD_DAO).insertAuditRecord(argCapture.capture());

        var audit = argCapture.getValue();

        assertThat(audit.getRecordId()).isEqualTo(id);
        assertThat(audit.getRecordType()).isEqualTo(User.class.getSimpleName());
        assertThat(audit.getAction()).isEqualTo(action);
    }

    @Nested
    class GetLoggedInUser {

        static {
            TestUserApp.userDao = USER_DAO;
            TestUserApp.auditRecordDao = AUDIT_RECORD_DAO;
            TestUserApp.errorDao = APPLICATION_ERROR_DAO;
        }

        static DropwizardAppExtension<AppConfig> EXT = new DropwizardAppExtension<>(
                TestUserApp.class,
                ResourceHelpers.resourceFilePath("config-unit-test.yml")
        );

        @Test
        void shouldReturnLoggedInUser() {
            var bob = User.builder()
                    .id(1L)
                    .firstName("Robert")
                    .lastName("Bob")
                    .systemIdentifier("bob")
                    .displayName("Robert Bob")
                    .build();

            when(USER_DAO.findBySystemIdentifier("bob")).thenReturn(Optional.of(bob));

            var loginResponse = EXT.client().target(f("http://localhost:{}/auth", EXT.getLocalPort()))
                    .path("/login")
                    .request()
                    .post(json(Map.of("username", "bob")));

            var token = loginResponse.getCookies().get("sessionToken");

            var response = EXT.client().target(f("http://localhost:{}/users", EXT.getLocalPort()))
                    .path("/current")
                    .request()
                    .cookie("sessionToken", token.getValue())
                    .get();

            assertOkResponse(response);

            var user = response.readEntity(User.class);

            assertThat(user)
                    .usingRecursiveComparison()
                    .ignoringFields("createdAt", "updatedAt")
                    .isEqualTo(bob);

        }

        @Test
        void shouldReturn404WhenUserIsNotFound() {
            var bob = User.builder()
                    .id(1L)
                    .firstName("Robert")
                    .lastName("Bob")
                    .systemIdentifier("bob")
                    .displayName("Robert Bob")
                    .build();

            when(USER_DAO.findBySystemIdentifier("bob"))
                    .thenReturn(Optional.of(bob))
                    .thenReturn(Optional.empty());

            var loginResponse = EXT.client().target(f("http://localhost:{}/auth", EXT.getLocalPort()))
                    .path("/login")
                    .request()
                    .post(json(Map.of("username", "bob")));

            var token = loginResponse.getCookies().get("sessionToken");

            var response = EXT.client().target(f("http://localhost:{}/users", EXT.getLocalPort()))
                    .path("/current")
                    .request()
                    .cookie("sessionToken", token.getValue())
                    .get();

            assertNotFoundResponse(response);

        }
    }

    @Nested
    class UpdateUser {

        @Test
        void shouldUpdateGivenUser() {
            when(USER_DAO.updateUser(any(User.class))).thenReturn(1);

            var user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .displayName("John Doe")
                .systemIdentifier("jdoe")
                .build();

            var response = APP.client().target("/users")
                .request()
                .put(json(user));

            assertAcceptedResponse(response);

            verify(USER_DAO).updateUser(any(User.class));
            
            verifyAuditRecorded(1L, Action.UPDATED);

            verifyNoMoreInteractions(USER_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAuditIfUpdateDoesNotChangeDB() {
            when(USER_DAO.updateUser(any(User.class))).thenReturn(0);
            
            var user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .displayName("John Doe")
                .systemIdentifier("jdoe")
                .build();

            var response = APP.client().target("/users")
                .request()
                .put(json(user));

            assertAcceptedResponse(response);

            verify(USER_DAO).updateUser(any(User.class));
            verifyNoMoreInteractions(USER_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }

        @Test
        void shouldReturn500WhenMissingId() {
            var user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .displayName("John Doe")
                .systemIdentifier("jdoe")
                .build();

            var response = APP.client().target("/users")
                .request()
                .put(json(user));

            assertInternalServerErrorResponse(response);

            verifyNoInteractions(USER_DAO, AUDIT_RECORD_DAO);
        }
    }
}
