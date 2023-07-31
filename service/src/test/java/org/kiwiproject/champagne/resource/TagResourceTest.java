package org.kiwiproject.champagne.resource;

import static javax.ws.rs.client.Entity.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertAcceptedResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertNoContentResponse;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.GenericType;
import java.util.List;
import java.util.Map;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.TagDao;
import org.kiwiproject.champagne.junit.jupiter.DeployableSystemExtension;
import org.kiwiproject.champagne.junit.jupiter.JwtExtension;
import org.kiwiproject.champagne.model.AuditRecord;
import org.kiwiproject.champagne.model.Tag;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;
import org.kiwiproject.dropwizard.error.test.junit.jupiter.ApplicationErrorExtension;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;
import org.mockito.ArgumentCaptor;

@DisplayName("TagResource")
@ExtendWith({DropwizardExtensionsSupport.class, ApplicationErrorExtension.class})
class TagResourceTest {

    private static final TagDao TAG_DAO = mock(TagDao.class);
    private static final AuditRecordDao AUDIT_RECORD_DAO = mock(AuditRecordDao.class);
    private static final ApplicationErrorDao APPLICATION_ERROR_DAO = mock(ApplicationErrorDao.class);

    private static final TagResource RESOURCE = new TagResource(TAG_DAO, AUDIT_RECORD_DAO, APPLICATION_ERROR_DAO);
    private static final ResourceExtension RESOURCES = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(RESOURCE)
            .addProvider(JaxrsExceptionMapper.class)
            .build();

    @RegisterExtension
    public final JwtExtension jwtExtension = new JwtExtension("bob");

    @RegisterExtension
    public final DeployableSystemExtension deployableSystemExtension = new DeployableSystemExtension(1L, true);


    @AfterEach
    void cleanup() {
        reset(TAG_DAO, AUDIT_RECORD_DAO);
    }

    @Nested
    class ListTagsForCurrentUser {

        @Test
        void shouldReturnListOfTags() {
            var tag = Tag.builder()
                    .id(1L)
                    .name("bob")
                    .build();

            when(TAG_DAO.findTagsForSystem(1L)).thenReturn(List.of(tag));

            var response = RESOURCES
                    .target("/tag")
                    .request()
                    .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<List<Tag>>() {
            });

            assertThat(result).hasSize(1);

            verify(TAG_DAO).findTagsForSystem(1L);
            verifyNoMoreInteractions(TAG_DAO);
        }
    }

    @Nested
    class CreateTag {
        @Test
        void shouldAddTheGivenHost() {
            when(TAG_DAO.insertTag(any(Tag.class))).thenReturn(1L);

            var tag = Tag.builder()
                    .name("core")
                    .deployableSystemId(1L)
                    .build();

            var response = RESOURCES.client().target("/tag")
                    .request()
                    .post(json(tag));

            assertAcceptedResponse(response);

            verify(TAG_DAO).insertTag(any(Tag.class));

            verifyAuditRecorded(AuditRecord.Action.CREATED);

            verifyNoMoreInteractions(TAG_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldAddTheGivenHostWithTheCurrentSystemWhenNotProvided() {
            when(TAG_DAO.insertTag(any(Tag.class))).thenReturn(1L);

            var tag = Tag.builder()
                    .name("core")
                    .build();

            var response = RESOURCES.client().target("/tag")
                    .request()
                    .post(json(tag));

            assertAcceptedResponse(response);

            verify(TAG_DAO).insertTag(any(Tag.class));

            verifyAuditRecorded(AuditRecord.Action.CREATED);

            verifyNoMoreInteractions(TAG_DAO, AUDIT_RECORD_DAO);
        }
    }

    @Nested
    class UpdateTag {

        @Test
        void shouldUpdateGivenHost() {
            when(TAG_DAO.updateTag(1L, "audit")).thenReturn(1);

            var response = RESOURCES.client().target("/tag")
                    .path("{id}")
                    .resolveTemplate("id", 1)
                    .request()
                    .put(json(Map.of("name", "audit")));

            assertAcceptedResponse(response);

            verify(TAG_DAO).updateTag(1L, "audit");

            verifyAuditRecorded(AuditRecord.Action.UPDATED);

            verifyNoMoreInteractions(TAG_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAuditUpdateIfDoesNotChangeAnything() {
            when(TAG_DAO.updateTag( 1L, "core")).thenReturn(0);

            var response = RESOURCES.client().target("/tag")
                    .path("{id}")
                    .resolveTemplate("id", 1)
                    .request()
                    .put(json(Map.of("name", "core")));

            assertAcceptedResponse(response);

            verify(TAG_DAO).updateTag( 1L, "core");
            verifyNoMoreInteractions(TAG_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }
    }


    @Nested
    class DeleteTag {

        @Test
        void shouldDeleteGivenTag() {
            when(TAG_DAO.deleteTag(1L)).thenReturn(1);

            var response = RESOURCES.client().target("/tag")
                    .path("{id}")
                    .resolveTemplate("id", 1)
                    .request()
                    .delete();

            assertNoContentResponse(response);

            verify(TAG_DAO).deleteTag(1L);

            verifyAuditRecorded(AuditRecord.Action.DELETED);

            verifyNoMoreInteractions(TAG_DAO, AUDIT_RECORD_DAO);
        }

        @Test
        void shouldNotAuditDeleteIfDoesNotChangeAnything() {
            when(TAG_DAO.deleteTag(1L)).thenReturn(0);

            var response = RESOURCES.client().target("/tag")
                    .path("{id}")
                    .resolveTemplate("id", 1)
                    .request()
                    .delete();

            assertNoContentResponse(response);

            verify(TAG_DAO).deleteTag(1L);
            verifyNoMoreInteractions(TAG_DAO);
            verifyNoInteractions(AUDIT_RECORD_DAO);
        }
    }

    private void verifyAuditRecorded(AuditRecord.Action action) {
        var argCapture = ArgumentCaptor.forClass(AuditRecord.class);
        verify(AUDIT_RECORD_DAO).insertAuditRecord(argCapture.capture());

        var audit = argCapture.getValue();

        assertThat(audit.getRecordId()).isEqualTo(1);
        assertThat(audit.getRecordType()).isEqualTo(Tag.class.getSimpleName());
        assertThat(audit.getAction()).isEqualTo(action);
    }

}
