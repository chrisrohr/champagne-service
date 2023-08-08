package org.kiwiproject.champagne.resource;

import static jakarta.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.champagne.util.JwtResourceHelper.generateJwt;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertAcceptedResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertNoContentResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertUnauthorizedResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.core.GenericType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.DeployableSystemDao;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.champagne.model.DeployableSystem;
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.champagne.util.JwtResourceHelper;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;
import org.kiwiproject.dropwizard.error.test.junit.jupiter.ApplicationErrorExtension;
import org.kiwiproject.spring.data.KiwiPage;

@DisplayName("DeployableSystemResource")
@ExtendWith({DropwizardExtensionsSupport.class, ApplicationErrorExtension.class})
class DeployableSystemResourceTest {

    private static final DeployableSystemDao DEPLOYABLE_SYSTEM_DAO = mock(DeployableSystemDao.class);
    private static final AuditRecordDao AUDIT_RECORD_DAO = mock(AuditRecordDao.class);
    private static final ApplicationErrorDao APPLICATION_ERROR_DAO = mock(ApplicationErrorDao.class);
    private static final UserDao USER_DAO = mock(UserDao.class);

    private static final DeployableSystemResource RESOURCE = new DeployableSystemResource(DEPLOYABLE_SYSTEM_DAO, USER_DAO, AUDIT_RECORD_DAO, APPLICATION_ERROR_DAO);
    private static final ResourceExtension RESOURCES = JwtResourceHelper.configureJwtResource(RESOURCE);

    @AfterEach
    void cleanup() {
        reset(DEPLOYABLE_SYSTEM_DAO, AUDIT_RECORD_DAO, USER_DAO);
    }

    @Nested
    class ListSystemsForCurrentUser {

        @Test
        void shouldReturnListOfSystemsUserHasAccessToWithAdminFlagSet() {
            var user = User.builder()
                    .id(1L)
                    .systemIdentifier("bob")
                    .build();

            when(USER_DAO.findBySystemIdentifier("bob")).thenReturn(Optional.of(user));

            var system = DeployableSystem.builder()
                    .id(1L)
                    .name("kiwi")
                    .devEnvironmentId(2L)
                    .environmentPromotionOrder("2,3,4")
                    .admin(true)
                    .build();

            when(DEPLOYABLE_SYSTEM_DAO.findDeployableSystemsForUser(1L)).thenReturn(List.of(system));

            var token = generateJwt(false);

            var response = RESOURCES
                    .target("/systems")
                    .request()
                    .cookie("sessionToken", token)
                    .get();


            assertOkResponse(response);

            var result = response.readEntity(new GenericType<List<DeployableSystem>>() {
            });

            assertThat(result).hasSize(1);

            verify(DEPLOYABLE_SYSTEM_DAO).findDeployableSystemsForUser(1L);
            verify(USER_DAO).findBySystemIdentifier("bob");

            verifyNoMoreInteractions(DEPLOYABLE_SYSTEM_DAO, USER_DAO);
        }

        @Test
        void shouldReturnUnauthorizedWhenUserCanNotBeFound() {
            when(USER_DAO.findBySystemIdentifier("bob")).thenReturn(Optional.empty());

            var token = generateJwt(false);
            var response = RESOURCES.client()
                    .target("/systems")
                    .request()
                    .cookie("sessionToken", token)
                    .get();

            assertUnauthorizedResponse(response);

            verify(USER_DAO).findBySystemIdentifier("bob");
            verifyNoMoreInteractions(USER_DAO);
            verifyNoInteractions(DEPLOYABLE_SYSTEM_DAO);
        }
    }

    @Nested
    class ListAllSystems {

        @Test
        void shouldReturnPagedListOfDeployableSystems() {
            var system = DeployableSystem.builder()
                    .id(1L)
                    .name("kiwi")
                    .devEnvironmentId(2L)
                    .environmentPromotionOrder("2,3,4")
                    .build();

            var systemUser = DeployableSystem.SystemUser.builder()
                    .userId(1L)
                    .admin(true)
                    .build();

            when(DEPLOYABLE_SYSTEM_DAO.findPagedDeployableSystems(0, 10)).thenReturn(List.of(system));
            when(DEPLOYABLE_SYSTEM_DAO.countDeployableSystems()).thenReturn(1L);
            when(DEPLOYABLE_SYSTEM_DAO.findUsersForSystem(1L)).thenReturn(List.of(systemUser));

            var token = generateJwt(true);
            var response = RESOURCES.client()
                    .target("/systems/admin")
                    .queryParam("pageNumber", 1)
                    .queryParam("pageSize", 10)
                    .request()
                    .cookie("sessionToken", token)
                    .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<KiwiPage<DeployableSystem>>() {
            });

            assertThat(result.getNumber()).isOne();
            assertThat(result.getTotalElements()).isOne();

            verify(DEPLOYABLE_SYSTEM_DAO).findPagedDeployableSystems(0, 10);
            verify(DEPLOYABLE_SYSTEM_DAO).countDeployableSystems();
            verify(DEPLOYABLE_SYSTEM_DAO).findUsersForSystem(1L);

            verifyNoMoreInteractions(DEPLOYABLE_SYSTEM_DAO);
        }

        @Test
        void shouldReturnPagedListOfReleasesWithDefaultPaging() {
            var system = DeployableSystem.builder()
                    .id(1L)
                    .name("kiwi")
                    .devEnvironmentId(2L)
                    .environmentPromotionOrder("2,3,4")
                    .build();

            var systemUser = DeployableSystem.SystemUser.builder()
                    .userId(1L)
                    .admin(true)
                    .build();

            when(DEPLOYABLE_SYSTEM_DAO.findPagedDeployableSystems(0, 25)).thenReturn(List.of(system));
            when(DEPLOYABLE_SYSTEM_DAO.countDeployableSystems()).thenReturn(1L);
            when(DEPLOYABLE_SYSTEM_DAO.findUsersForSystem(1L)).thenReturn(List.of(systemUser));

            var token = generateJwt(true);
            var response = RESOURCES.client()
                    .target("/systems/admin")
                    .request()
                    .cookie("sessionToken", token)
                    .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<KiwiPage<DeployableSystem>>() {
            });

            assertThat(result.getNumber()).isOne();
            assertThat(result.getTotalElements()).isOne();

            verify(DEPLOYABLE_SYSTEM_DAO).findPagedDeployableSystems(0, 25);
            verify(DEPLOYABLE_SYSTEM_DAO).countDeployableSystems();
            verify(DEPLOYABLE_SYSTEM_DAO).findUsersForSystem(1L);

            verifyNoMoreInteractions(DEPLOYABLE_SYSTEM_DAO);
        }
    }

    @Nested
    class CreateSystem {

        @Test
        void shouldSaveNewSystem() {
            var system = DeployableSystem.builder()
                    .name("kiwi")
                    .build();

            when(DEPLOYABLE_SYSTEM_DAO.insertDeployableSystem(any(DeployableSystem.class))).thenReturn(1L);

            var token = generateJwt(true);
            var response = RESOURCES.client()
                    .target("/systems")
                    .request()
                    .cookie("sessionToken", token)
                    .post(json(system));

            assertAcceptedResponse(response);

            verify(DEPLOYABLE_SYSTEM_DAO).insertDeployableSystem(any(DeployableSystem.class));
            verify(AUDIT_RECORD_DAO).insertAuditRecord(any(AuditRecord.class));

            verifyNoMoreInteractions(DEPLOYABLE_SYSTEM_DAO, AUDIT_RECORD_DAO);
        }
    }

    @Nested
    class SetDevEnvironmentOnSystem {

        @Test
        void shouldUpdateTheDevEnvironmentForTheGivenSystem() {
            var user = User.builder()
                    .id(1L)
                    .build();

            when(USER_DAO.findBySystemIdentifier("bob")).thenReturn(Optional.of(user));
            when(DEPLOYABLE_SYSTEM_DAO.isUserAdminOfSystem(1L, 2L)).thenReturn(true);
            when(DEPLOYABLE_SYSTEM_DAO.updateDevEnvironment(2L, 3L)).thenReturn(1);

            var token = generateJwt(true);
            var response = RESOURCES.client()
                    .target("/systems/2/dev/3")
                    .request()
                    .cookie("sessionToken", token)
                    .put(json(""));

            assertAcceptedResponse(response);

            verify(USER_DAO).findBySystemIdentifier("bob");
            verify(DEPLOYABLE_SYSTEM_DAO).isUserAdminOfSystem(1L, 2L);
            verify(DEPLOYABLE_SYSTEM_DAO).updateDevEnvironment(2L, 3L);
            verify(AUDIT_RECORD_DAO).insertAuditRecord(any(AuditRecord.class));

            verifyNoMoreInteractions(USER_DAO, DEPLOYABLE_SYSTEM_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAuditActionWhenNothingChanges() {
            var user = User.builder()
                    .id(1L)
                    .build();

            when(USER_DAO.findBySystemIdentifier("bob")).thenReturn(Optional.of(user));
            when(DEPLOYABLE_SYSTEM_DAO.isUserAdminOfSystem(1L, 2L)).thenReturn(true);
            when(DEPLOYABLE_SYSTEM_DAO.updateDevEnvironment(2L, 3L)).thenReturn(0);

            var token = generateJwt(true);
            var response = RESOURCES.client()
                    .target("/systems/2/dev/3")
                    .request()
                    .cookie("sessionToken", token)
                    .put(json(""));

            assertAcceptedResponse(response);

            verify(USER_DAO).findBySystemIdentifier("bob");
            verify(DEPLOYABLE_SYSTEM_DAO).isUserAdminOfSystem(1L, 2L);
            verify(DEPLOYABLE_SYSTEM_DAO).updateDevEnvironment(2L, 3L);

            verifyNoMoreInteractions(USER_DAO, DEPLOYABLE_SYSTEM_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }

        @Test
        void shouldThrow403WhenUserIsNotAdminInSystem() {
            var user = User.builder()
                    .id(1L)
                    .build();

            when(USER_DAO.findBySystemIdentifier("bob")).thenReturn(Optional.of(user));
            when(DEPLOYABLE_SYSTEM_DAO.isUserAdminOfSystem(1L, 2L)).thenReturn(false);

            var token = generateJwt(true);
            var response = RESOURCES.client()
                    .target("/systems/2/dev/3")
                    .request()
                    .cookie("sessionToken", token)
                    .put(json(""));

            assertUnauthorizedResponse(response);

            verify(USER_DAO).findBySystemIdentifier("bob");
            verify(DEPLOYABLE_SYSTEM_DAO).isUserAdminOfSystem(1L, 2L);

            verifyNoMoreInteractions(USER_DAO, DEPLOYABLE_SYSTEM_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }

        @Test
        void shouldThrow403WhenUserIsNotFound() {
            when(USER_DAO.findBySystemIdentifier("bob")).thenReturn(Optional.empty());

            var token = generateJwt(true);
            var response = RESOURCES.client()
                    .target("/systems/2/dev/3")
                    .request()
                    .cookie("sessionToken", token)
                    .put(json(""));

            assertUnauthorizedResponse(response);

            verify(USER_DAO).findBySystemIdentifier("bob");

            verifyNoMoreInteractions(USER_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO, DEPLOYABLE_SYSTEM_DAO);
        }
    }

    @Nested
    class SetEnvPromotionOrderOnSystem {

        @Test
        void shouldUpdateTheEnvOrderForTheGivenSystem() {
            var user = User.builder()
                    .id(1L)
                    .build();

            when(USER_DAO.findBySystemIdentifier("bob")).thenReturn(Optional.of(user));
            when(DEPLOYABLE_SYSTEM_DAO.isUserAdminOfSystem(1L, 2L)).thenReturn(true);
            when(DEPLOYABLE_SYSTEM_DAO.updateEnvironmentPromotionOrder(2L, "1,2,3")).thenReturn(1);

            var token = generateJwt(true);
            var response = RESOURCES.client()
                    .target("/systems/2/order")
                    .request()
                    .cookie("sessionToken", token)
                    .put(json(List.of(1, 2, 3)));

            assertAcceptedResponse(response);

            verify(USER_DAO).findBySystemIdentifier("bob");
            verify(DEPLOYABLE_SYSTEM_DAO).isUserAdminOfSystem(1L, 2L);
            verify(DEPLOYABLE_SYSTEM_DAO).updateEnvironmentPromotionOrder(2L, "1,2,3");
            verify(AUDIT_RECORD_DAO).insertAuditRecord(any(AuditRecord.class));

            verifyNoMoreInteractions(USER_DAO, DEPLOYABLE_SYSTEM_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAuditActionWhenNothingChanges() {
            var user = User.builder()
                    .id(1L)
                    .build();

            when(USER_DAO.findBySystemIdentifier("bob")).thenReturn(Optional.of(user));
            when(DEPLOYABLE_SYSTEM_DAO.isUserAdminOfSystem(1L, 2L)).thenReturn(true);
            when(DEPLOYABLE_SYSTEM_DAO.updateEnvironmentPromotionOrder(2L, "1,2,3")).thenReturn(0);

            var token = generateJwt(true);
            var response = RESOURCES.client()
                    .target("/systems/2/order")
                    .request()
                    .cookie("sessionToken", token)
                    .put(json(List.of(1, 2, 3)));

            assertAcceptedResponse(response);

            verify(USER_DAO).findBySystemIdentifier("bob");
            verify(DEPLOYABLE_SYSTEM_DAO).isUserAdminOfSystem(1L, 2L);
            verify(DEPLOYABLE_SYSTEM_DAO).updateEnvironmentPromotionOrder(2L, "1,2,3");

            verifyNoMoreInteractions(USER_DAO, DEPLOYABLE_SYSTEM_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }

        @Test
        void shouldThrow403WhenUserIsNotAdminInSystem() {
            var user = User.builder()
                    .id(1L)
                    .build();

            when(USER_DAO.findBySystemIdentifier("bob")).thenReturn(Optional.of(user));
            when(DEPLOYABLE_SYSTEM_DAO.isUserAdminOfSystem(1L, 2L)).thenReturn(false);

            var token = generateJwt(true);
            var response = RESOURCES.client()
                    .target("/systems/2/order")
                    .request()
                    .cookie("sessionToken", token)
                    .put(json(List.of(1, 2, 3)));

            assertUnauthorizedResponse(response);

            verify(USER_DAO).findBySystemIdentifier("bob");
            verify(DEPLOYABLE_SYSTEM_DAO).isUserAdminOfSystem(1L, 2L);

            verifyNoMoreInteractions(USER_DAO, DEPLOYABLE_SYSTEM_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }

        @Test
        void shouldThrow403WhenUserIsNotFound() {
            when(USER_DAO.findBySystemIdentifier("bob")).thenReturn(Optional.empty());

            var token = generateJwt(true);
            var response = RESOURCES.client()
                    .target("/systems/2/order")
                    .request()
                    .cookie("sessionToken", token)
                    .put(json(List.of(1, 2, 3)));

            assertUnauthorizedResponse(response);

            verify(USER_DAO).findBySystemIdentifier("bob");

            verifyNoMoreInteractions(USER_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO, DEPLOYABLE_SYSTEM_DAO);
        }
    }

    @Nested
    class DeleteSystem {

        @Test
        void shouldDeleteGivenSystem() {
            when(DEPLOYABLE_SYSTEM_DAO.deleteById(1L)).thenReturn(1);

            var token = generateJwt(true);
            var response = RESOURCES.client().target("/systems")
                    .path("{id}")
                    .resolveTemplate("id", 1)
                    .request()
                    .cookie("sessionToken", token)
                    .delete();

            assertNoContentResponse(response);

            verify(DEPLOYABLE_SYSTEM_DAO).deleteById(1L);
            verify(AUDIT_RECORD_DAO).insertAuditRecord(any(AuditRecord.class));

            verifyNoMoreInteractions(DEPLOYABLE_SYSTEM_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAuditIfDeleteDoesNotChangeDB() {
            when(DEPLOYABLE_SYSTEM_DAO.deleteById(1L)).thenReturn(0);

            var token = generateJwt(true);
            var response = RESOURCES.client().target("/systems")
                    .path("{id}")
                    .resolveTemplate("id", 1)
                    .request()
                    .cookie("sessionToken", token)
                    .delete();

            assertNoContentResponse(response);

            verify(DEPLOYABLE_SYSTEM_DAO).deleteById(1L);
            verifyNoMoreInteractions(DEPLOYABLE_SYSTEM_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }
    }

    @Nested
    class AddUserToSystem {

        @Test
        void shouldAddGivenUserToGivenSystem() {
            var systemUser = DeployableSystem.SystemUser.builder()
                    .userId(2L)
                    .admin(false)
                    .build();

            var token = generateJwt(true);
            var response = RESOURCES.client().target("/systems/{id}/user")
                    .resolveTemplate("id", 1L)
                    .request()
                    .cookie("sessionToken", token)
                    .post(json(systemUser));

            assertAcceptedResponse(response);

            verify(DEPLOYABLE_SYSTEM_DAO).insertOrUpdateSystemUser(1L, 2L, false);
        }
    }

    @Nested
    class RemoveUserFromSystem {

        @Test
        void shouldRemoveUserSystemLinkMatchingGivenSystemIdAndUserId() {
            var token = generateJwt(true);
            var response = RESOURCES.client().target("/systems/{systemId}/users/{userId}")
                    .resolveTemplate("systemId", 1L)
                    .resolveTemplate("userId", 1L)
                    .request()
                    .cookie("sessionToken", token)
                    .delete();

            assertNoContentResponse(response);

            verify(DEPLOYABLE_SYSTEM_DAO).deleteUserFromSystem(1L, 1L);
        }
    }
}
