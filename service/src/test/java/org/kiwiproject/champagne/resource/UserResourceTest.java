package org.kiwiproject.champagne.resource;

import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertNoContentResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertResponseStatusCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.util.AuthHelper;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;
import org.kiwiproject.spring.data.KiwiPage;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import javax.ws.rs.core.GenericType;

@DisplayName("UserResource")
@ExtendWith({ DropwizardExtensionsSupport.class, SoftAssertionsExtension.class })
public class UserResourceTest {

    private static final UserDao USER_DAO = mock(UserDao.class);
    private static final AuditRecordDao AUDIT_RECORD_DAO = mock(AuditRecordDao.class);
    private static final UserResource USER_RESOURCE = new UserResource(USER_DAO, AUDIT_RECORD_DAO);

    private static final ResourceExtension APP = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(USER_RESOURCE)
            .addProvider(JaxrsExceptionMapper.class)
            .build();
    
    @BeforeEach
    void setUp() {
        reset(USER_DAO, AUDIT_RECORD_DAO);

        AuthHelper.setupCurrentPrincipalFor("bob");
    }

    @AfterEach
    void tearDown() {
        AuthHelper.removePrincipal();
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
    }

    private void verifyAuditRecorded(long id, Action action) {
        var argCapture = ArgumentCaptor.forClass(AuditRecord.class);
        verify(AUDIT_RECORD_DAO).insertAuditRecord(argCapture.capture());

        var audit = argCapture.getValue();

        assertThat(audit.getRecordId()).isEqualTo(id);
        assertThat(audit.getRecordType()).isEqualTo(User.class.getSimpleName());
        assertThat(audit.getAction()).isEqualTo(action);
    }
}
