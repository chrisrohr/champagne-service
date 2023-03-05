package org.kiwiproject.champagne.resource;

import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertAcceptedResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertBadRequest;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.DeploymentEnvironmentDao;
import org.kiwiproject.champagne.junit.jupiter.DeployableSystemExtension;
import org.kiwiproject.champagne.junit.jupiter.JwtExtension;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;
import org.kiwiproject.champagne.model.DeploymentEnvironment;
import org.kiwiproject.champagne.service.ManualTaskService;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;
import org.kiwiproject.dropwizard.error.test.junit.jupiter.ApplicationErrorExtension;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;
import org.mockito.ArgumentCaptor;

import java.util.List;
import javax.ws.rs.core.GenericType;

@DisplayName("DeploymentEnvironmentResource")
@ExtendWith({DropwizardExtensionsSupport.class, ApplicationErrorExtension.class, DeployableSystemExtension.class})
class DeploymentEnvironmentResourceTest {
    private static final DeploymentEnvironmentDao DEPLOYMENT_ENVIRONMENT_DAO = mock(DeploymentEnvironmentDao.class);
    private static final AuditRecordDao AUDIT_RECORD_DAO = mock(AuditRecordDao.class);
    private static final ManualTaskService MANUAL_TASK_SERVICE = mock(ManualTaskService.class);
    private static final ApplicationErrorDao APPLICATION_ERROR_DAO = mock(ApplicationErrorDao.class);
    private static final DeploymentEnvironmentResource DEPLOYMENT_ENVIRONMENT_RESOURCE = new DeploymentEnvironmentResource(DEPLOYMENT_ENVIRONMENT_DAO, AUDIT_RECORD_DAO, APPLICATION_ERROR_DAO, MANUAL_TASK_SERVICE);

    private static final ResourceExtension APP = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(DEPLOYMENT_ENVIRONMENT_RESOURCE)
            .addProvider(JaxrsExceptionMapper.class)
            .build();

    @RegisterExtension
    private final JwtExtension jwtExtension = new JwtExtension("bob");

    @AfterEach
    void tearDown() {
        reset(DEPLOYMENT_ENVIRONMENT_DAO, AUDIT_RECORD_DAO, MANUAL_TASK_SERVICE);
    }

    @Nested
    class ListEnvironments {

        @Test
        void shouldReturnListOfEnvironments() {
            var env = DeploymentEnvironment.builder()
                    .name("DEV")
                    .deployableSystemId(1L)
                    .build();

            when(DEPLOYMENT_ENVIRONMENT_DAO.findAllEnvironments(1L)).thenReturn(List.of(env));

            var response = APP.client().target("/environments")
                    .request()
                    .get();

            assertOkResponse(response);

            var envs = response.readEntity(new GenericType<List<DeploymentEnvironment>>() {
            });

            var foundEnv = first(envs);
            assertThat(foundEnv)
                    .usingRecursiveComparison()
                    .ignoringFields("createdAt", "updatedAt")
                    .isEqualTo(env);

            verify(DEPLOYMENT_ENVIRONMENT_DAO).findAllEnvironments(1L);
            verifyNoMoreInteractions(DEPLOYMENT_ENVIRONMENT_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }
    }

    @Nested
    class CreateEnvironment {

        @Test
        void shouldAddTheGivenEnvironment() {
            when(DEPLOYMENT_ENVIRONMENT_DAO.insertEnvironment(any(DeploymentEnvironment.class))).thenReturn(1L);

            var env = DeploymentEnvironment.builder()
                    .name("DEV")
                    .deployableSystemId(1L)
                    .build();

            var response = APP.client().target("/environments")
                    .request()
                    .post(json(env));

            assertNoContentResponse(response);

            verify(DEPLOYMENT_ENVIRONMENT_DAO).insertEnvironment(isA(DeploymentEnvironment.class));

            verifyAuditRecorded(Action.CREATED);

            verifyNoMoreInteractions(DEPLOYMENT_ENVIRONMENT_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAddTheGivenUserWhenValidationErrors() {
            var env = DeploymentEnvironment.builder().build();

            var response = APP.client().target("/environments")
                    .request()
                    .post(json(env));

            assertResponseStatusCode(response, 422);

            verifyNoInteractions(DEPLOYMENT_ENVIRONMENT_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldSetTheSystemFromHeaderWhenNotPosted() {
            when(DEPLOYMENT_ENVIRONMENT_DAO.insertEnvironment(any(DeploymentEnvironment.class))).thenReturn(1L);

            var env = DeploymentEnvironment.builder()
                    .name("DEV")
                    .build();

            var response = APP.client().target("/environments")
                    .request()
                    .post(json(env));

            assertNoContentResponse(response);

            verify(DEPLOYMENT_ENVIRONMENT_DAO).insertEnvironment(isA(DeploymentEnvironment.class));

            verifyAuditRecorded(Action.CREATED);

            verifyNoMoreInteractions(DEPLOYMENT_ENVIRONMENT_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldReturnBadRequestWhenSystemNotProvided() {
            DeployableSystemThreadLocal.clearDeployableSystem();

            var env = DeploymentEnvironment.builder()
                    .name("DEV")
                    .build();

            var response = APP.client().target("/environments")
                    .request()
                    .post(json(env));

            assertBadRequest(response);

            verifyNoInteractions(DEPLOYMENT_ENVIRONMENT_DAO, AUDIT_RECORD_DAO);
        }
    }

    @Nested
    class UpdateEnvironment {

        @Test
        void shouldUpdateTheGivenEnvironment() {
            when(DEPLOYMENT_ENVIRONMENT_DAO.updateEnvironment(any(DeploymentEnvironment.class))).thenReturn(1);

            var env = DeploymentEnvironment.builder()
                    .id(1L)
                    .name("DEV")
                    .build();

            var response = APP.client().target("/environments")
                    .request()
                    .put(json(env));

            assertAcceptedResponse(response);

            verify(DEPLOYMENT_ENVIRONMENT_DAO).updateEnvironment(isA(DeploymentEnvironment.class));

            verifyAuditRecorded(Action.UPDATED);

            verifyNoMoreInteractions(DEPLOYMENT_ENVIRONMENT_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotUpdateTheGivenEnvironmentWhenIdIsMissing() {
            var env = DeploymentEnvironment.builder().build();

            var response = APP.client().target("/environments")
                    .request()
                    .put(json(env));

            assertResponseStatusCode(response, 422);

            verifyNoInteractions(DEPLOYMENT_ENVIRONMENT_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAuditUpdateIfUpdateDoesNotChangeAnything() {
            when(DEPLOYMENT_ENVIRONMENT_DAO.updateEnvironment(any(DeploymentEnvironment.class))).thenReturn(0);

            var env = DeploymentEnvironment.builder()
                    .id(1L)
                    .name("DEV")
                    .build();

            var response = APP.client().target("/environments")
                    .request()
                    .put(json(env));

            assertAcceptedResponse(response);

            verify(DEPLOYMENT_ENVIRONMENT_DAO).updateEnvironment(isA(DeploymentEnvironment.class));
            verifyNoMoreInteractions(DEPLOYMENT_ENVIRONMENT_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }
    }

    @Nested
    class HardDeleteEnvironment {

        @Test
        void shouldDeleteGivenEnvironment() {
            when(DEPLOYMENT_ENVIRONMENT_DAO.hardDeleteById(1L)).thenReturn(1);

            var response = APP.client().target("/environments")
                    .path("{id}/delete")
                    .resolveTemplate("id", 1)
                    .request()
                    .delete();

            assertAcceptedResponse(response);

            verify(DEPLOYMENT_ENVIRONMENT_DAO).hardDeleteById(1L);

            verifyAuditRecorded(Action.DELETED);

            verifyNoMoreInteractions(DEPLOYMENT_ENVIRONMENT_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAuditDeleteIfDoesNotChangeAnything() {
            when(DEPLOYMENT_ENVIRONMENT_DAO.hardDeleteById(1L)).thenReturn(0);

            var response = APP.client().target("/environments")
                    .path("{id}/delete")
                    .resolveTemplate("id", 1)
                    .request()
                    .delete();

            assertAcceptedResponse(response);

            verify(DEPLOYMENT_ENVIRONMENT_DAO).hardDeleteById(1L);
            verifyNoMoreInteractions(DEPLOYMENT_ENVIRONMENT_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }
    }

    @Nested
    class DeactivateEnvironment {

        @Test
        void shouldSoftDeleteGivenEnvironment() {
            when(DEPLOYMENT_ENVIRONMENT_DAO.softDeleteById(1L)).thenReturn(1);

            var response = APP.client().target("/environments")
                    .path("{id}/deactivate")
                    .resolveTemplate("id", 1)
                    .request()
                    .delete();

            assertAcceptedResponse(response);

            verify(DEPLOYMENT_ENVIRONMENT_DAO).softDeleteById(1L);

            verifyAuditRecorded(Action.UPDATED);

            verifyNoMoreInteractions(DEPLOYMENT_ENVIRONMENT_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAuditSoftDeleteIfDoesNotChangeAnything() {
            when(DEPLOYMENT_ENVIRONMENT_DAO.softDeleteById(1L)).thenReturn(0);

            var response = APP.client().target("/environments")
                    .path("{id}/deactivate")
                    .resolveTemplate("id", 1)
                    .request()
                    .delete();

            assertAcceptedResponse(response);

            verify(DEPLOYMENT_ENVIRONMENT_DAO).softDeleteById(1L);
            verifyNoMoreInteractions(DEPLOYMENT_ENVIRONMENT_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }
    }

    @Nested
    class ActivateEnvironment {

        @Test
        void shouldUnDeleteGivenEnvironment() {
            when(DEPLOYMENT_ENVIRONMENT_DAO.unSoftDeleteById(1L)).thenReturn(1);

            var response = APP.client().target("/environments")
                    .path("{id}/activate")
                    .resolveTemplate("id", 1)
                    .request()
                    .put(json(""));

            assertAcceptedResponse(response);

            verify(DEPLOYMENT_ENVIRONMENT_DAO).unSoftDeleteById(1L);

            verifyAuditRecorded(Action.UPDATED);

            verifyNoMoreInteractions(DEPLOYMENT_ENVIRONMENT_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAuditUnDeleteIfDoesNotChangeAnything() {
            when(DEPLOYMENT_ENVIRONMENT_DAO.softDeleteById(1L)).thenReturn(0);

            var response = APP.client().target("/environments")
                    .path("{id}/activate")
                    .resolveTemplate("id", 1)
                    .request()
                    .put(json(""));

            assertAcceptedResponse(response);

            verify(DEPLOYMENT_ENVIRONMENT_DAO).unSoftDeleteById(1L);
            verifyNoMoreInteractions(DEPLOYMENT_ENVIRONMENT_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }
    }

    private void verifyAuditRecorded(Action action) {
        var argCapture = ArgumentCaptor.forClass(AuditRecord.class);
        verify(AUDIT_RECORD_DAO).insertAuditRecord(argCapture.capture());

        var audit = argCapture.getValue();

        assertThat(audit.getRecordId()).isEqualTo(1);
        assertThat(audit.getRecordType()).isEqualTo(DeploymentEnvironment.class.getSimpleName());
        assertThat(audit.getAction()).isEqualTo(action);
    }
}
