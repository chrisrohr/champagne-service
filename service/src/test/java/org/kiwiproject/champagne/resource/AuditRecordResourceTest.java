package org.kiwiproject.champagne.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.core.GenericType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.junit.jupiter.DeployableSystemExtension;
import org.kiwiproject.champagne.junit.jupiter.JwtExtension;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.dropwizard.util.exception.JerseyViolationExceptionMapper;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;
import org.kiwiproject.spring.data.KiwiPage;

@DisplayName("AuditRecordResource")
@ExtendWith({DropwizardExtensionsSupport.class, DeployableSystemExtension.class})
class AuditRecordResourceTest {

    private static final AuditRecordDao AUDIT_RECORD_DAO = mock(AuditRecordDao.class);
    private static final AuditRecordResource RESOURCE = new AuditRecordResource(AUDIT_RECORD_DAO);

    private static final ResourceExtension RESOURCES = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(RESOURCE)
            .addProvider(JerseyViolationExceptionMapper.class)
            .addProvider(JaxrsExceptionMapper.class)
            .build();

    @RegisterExtension
    private final JwtExtension jwtExtension = new JwtExtension("bob");

    @AfterEach
    void cleanup() {
        reset(AUDIT_RECORD_DAO);
    }

    @Nested
    class GetPagedAudits {

        @Test
        void shouldReturnPagedListOfAuditRecords() {
            var auditRecord = AuditRecord.builder()
                    .id(1L)
                    .recordId(42L)
                    .recordType(User.class.getSimpleName())
                    .action(Action.CREATED)
                    .deployableSystemId(1L)
                    .build();

            when(AUDIT_RECORD_DAO.findPagedAuditRecords(0, 10, 1L)).thenReturn(List.of(auditRecord));
            when(AUDIT_RECORD_DAO.countAuditRecords(1L)).thenReturn(1L);

            var response = RESOURCES.client()
                    .target("/audit")
                    .queryParam("pageNumber", 1)
                    .queryParam("pageSize", 10)
                    .request()
                    .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<KiwiPage<AuditRecord>>() {
            });

            assertThat(result.getNumber()).isOne();
            assertThat(result.getTotalElements()).isOne();

            var audit = first(result.getContent());
            assertThat(audit)
                    .usingRecursiveComparison()
                    .ignoringFields("timestamp")
                    .isEqualTo(auditRecord);

            verify(AUDIT_RECORD_DAO).findPagedAuditRecords(0, 10, 1L);
            verify(AUDIT_RECORD_DAO).countAuditRecords(1L);

            verifyNoMoreInteractions(AUDIT_RECORD_DAO);
        }

        @Test
        void shouldReturnPagedListOfAuditRecordsWithDefaultPaging() {
            var auditRecord = AuditRecord.builder()
                    .id(1L)
                    .recordId(42L)
                    .recordType(User.class.getSimpleName())
                    .action(Action.CREATED)
                    .deployableSystemId(1L)
                    .build();

            when(AUDIT_RECORD_DAO.findPagedAuditRecords(0, 50, 1L)).thenReturn(List.of(auditRecord));
            when(AUDIT_RECORD_DAO.countAuditRecords(1L)).thenReturn(1L);

            var response = RESOURCES.client()
                    .target("/audit")
                    .request()
                    .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<KiwiPage<AuditRecord>>() {
            });

            assertThat(result.getNumber()).isOne();
            assertThat(result.getTotalElements()).isOne();

            var audit = first(result.getContent());
            assertThat(audit)
                    .usingRecursiveComparison()
                    .ignoringFields("timestamp")
                    .isEqualTo(auditRecord);

            verify(AUDIT_RECORD_DAO).findPagedAuditRecords(0, 50, 1L);
            verify(AUDIT_RECORD_DAO).countAuditRecords(1L);

            verifyNoMoreInteractions(AUDIT_RECORD_DAO);
        }
    }

}
