package org.kiwiproject.champagne.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;

import java.util.List;

import javax.ws.rs.core.GenericType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.champagne.model.User;
import org.kiwiproject.champagne.model.AuditRecord.Action;
import org.kiwiproject.champagne.util.AuthHelper;
import org.kiwiproject.dropwizard.util.exception.JerseyViolationExceptionMapper;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;
import org.kiwiproject.spring.data.KiwiPage;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

@DisplayName("AuditRecordResource")
@ExtendWith(DropwizardExtensionsSupport.class)
class AuditRecordResourceTest {
    
    private static final AuditRecordDao AUDIT_RECORD_DAO = mock(AuditRecordDao.class);
    private static final AuditRecordResource RESOURCE = new AuditRecordResource(AUDIT_RECORD_DAO);

    private static final ResourceExtension RESOURCES = ResourceExtension.builder()
        .bootstrapLogging(false)
        .addResource(RESOURCE)
        .addProvider(JerseyViolationExceptionMapper.class)
        .addProvider(JaxrsExceptionMapper.class)
        .build();

    @BeforeEach
    void setUp() {
        AuthHelper.setupCurrentPrincipalFor("bob");
    }

    @AfterEach
    void cleanup() {
        reset(AUDIT_RECORD_DAO);
        AuthHelper.removePrincipal();
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
                .build();

            when(AUDIT_RECORD_DAO.findPagedAuditRecords(0, 10)).thenReturn(List.of(auditRecord));
            when(AUDIT_RECORD_DAO.countAuditRecords()).thenReturn(1L);

            var response = RESOURCES.client()
                .target("/audit")
                .queryParam("pageNumber", 1)
                .queryParam("pageSize", 10)
                .request()
                .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<KiwiPage<AuditRecord>>(){});

            assertThat(result.getNumber()).isOne();
            assertThat(result.getTotalElements()).isOne();

            var audit = first(result.getContent());
            assertThat(audit)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(auditRecord);

            verify(AUDIT_RECORD_DAO).findPagedAuditRecords(0, 10);
            verify(AUDIT_RECORD_DAO).countAuditRecords();

            verifyNoMoreInteractions(AUDIT_RECORD_DAO);
        }

        @Test
        void shouldReturnPagedListOfAuditRecordsWithDefaultPaging() {
            var auditRecord = AuditRecord.builder()
                .id(1L)
                .recordId(42L)
                .recordType(User.class.getSimpleName())
                .action(Action.CREATED)
                .build();

            when(AUDIT_RECORD_DAO.findPagedAuditRecords(0, 50)).thenReturn(List.of(auditRecord));
            when(AUDIT_RECORD_DAO.countAuditRecords()).thenReturn(1L);

            var response = RESOURCES.client()
                .target("/audit")
                .request()
                .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<KiwiPage<AuditRecord>>(){});

            assertThat(result.getNumber()).isOne();
            assertThat(result.getTotalElements()).isOne();

            var audit = first(result.getContent());
            assertThat(audit)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(auditRecord);

            verify(AUDIT_RECORD_DAO).findPagedAuditRecords(0, 50);
            verify(AUDIT_RECORD_DAO).countAuditRecords();

            verifyNoMoreInteractions(AUDIT_RECORD_DAO);
        }
    }
    
}
