package org.kiwiproject.champagne.resource;

import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertAcceptedResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.HostDao;
import org.kiwiproject.champagne.junit.jupiter.JwtExtension;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.model.DeploymentEnvironment;
import org.kiwiproject.champagne.model.Host;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;
import org.mockito.ArgumentCaptor;

import java.util.List;
import javax.ws.rs.core.GenericType;

@DisplayName("HostConfigurationResource")
@ExtendWith( DropwizardExtensionsSupport.class)
class HostConfigurationResourceTest {
    private static final HostDao HOST_DAO = mock(HostDao.class);
    private static final AuditRecordDao AUDIT_RECORD_DAO = mock(AuditRecordDao.class);
    private static final HostConfigurationResource HOST_CONFIGURATION_RESOURCE = new HostConfigurationResource(HOST_DAO, AUDIT_RECORD_DAO);

    private static final ResourceExtension APP = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(HOST_CONFIGURATION_RESOURCE)
            .addProvider(JaxrsExceptionMapper.class)
            .build();

    @RegisterExtension
    private final JwtExtension jwtExtension = new JwtExtension("bob");
    
    @BeforeEach
    void setUp() {
        reset(HOST_DAO, AUDIT_RECORD_DAO);
    }

    @Nested
    class ListHostsForEnvironment {

        @Test
        void shouldReturnListOfHostsForEnv() {
            var host = Host.builder()
                .id(1L)
                .hostname("localhost")
                .build();

            when(HOST_DAO.findHostsByEnvId(1L)).thenReturn(List.of(host));

            var response = APP.client().target("/host/{envId}")
                .resolveTemplate("envId", 1L)
                .request()
                .get();

            assertOkResponse(response);

            var hosts = response.readEntity(new GenericType<List<Host>>(){});

            var foundHost = first(hosts);
            assertThat(foundHost)
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt")
                .isEqualTo(host);

            verify(HOST_DAO).findHostsByEnvId(1L);
            verifyNoMoreInteractions(HOST_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }
    }

    @Nested
    class CreateHost {

        @Test
        void shouldAddTheGivenHost() {
            when(HOST_DAO.insertHost(any(Host.class), anyString())).thenReturn(1L);

            var host = Host.builder()
                .hostname("localhost")
                .tags(List.of("foo"))
                .build();

            var response = APP.client().target("/host")
                .request()
                .post(json(host));

            assertAcceptedResponse(response);

            verify(HOST_DAO).insertHost(any(Host.class), anyString());

            verifyAuditRecorded(1L, Action.CREATED);

            verifyNoMoreInteractions(HOST_DAO, AUDIT_RECORD_DAO);
        }
    }

    @Nested
    class DeleteHost {

        @Test
        void shouldDeleteGivenHost() {
            when(HOST_DAO.deleteHost(1L)).thenReturn(1);

            var response = APP.client().target("/host")
                .path("{id}")
                .resolveTemplate("id", 1)
                .request()
                .delete();

            assertAcceptedResponse(response);

            verify(HOST_DAO).deleteHost(1L);
            
            verifyAuditRecorded(1L, Action.DELETED);

            verifyNoMoreInteractions(HOST_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAuditDeleteIfDoesNotChangeAnything() {
            when(HOST_DAO.deleteHost(1L)).thenReturn(0);

            var response = APP.client().target("/host")
                .path("{id}")
                .resolveTemplate("id", 1)
                .request()
                .delete();

            assertAcceptedResponse(response);

            verify(HOST_DAO).deleteHost(1L);
            verifyNoMoreInteractions(HOST_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }
    }

    private void verifyAuditRecorded(long id, Action action) {
        var argCapture = ArgumentCaptor.forClass(AuditRecord.class);
        verify(AUDIT_RECORD_DAO).insertAuditRecord(argCapture.capture());

        var audit = argCapture.getValue();

        assertThat(audit.getRecordId()).isEqualTo(id);
        assertThat(audit.getRecordType()).isEqualTo(Host.class.getSimpleName());
        assertThat(audit.getAction()).isEqualTo(action);
    }
}
