package org.kiwiproject.champagne.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.core.GenericType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.dao.BuildDao;
import org.kiwiproject.champagne.junit.jupiter.DeployableSystemExtension;
import org.kiwiproject.champagne.junit.jupiter.JwtExtension;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;

@DisplayName("MetricsResource")
@ExtendWith({DropwizardExtensionsSupport.class})
class MetricsResourceTest {

    private static final BuildDao BUILD_DAO = mock(BuildDao.class);

    private static final MetricsResource RESOURCE = new MetricsResource(BUILD_DAO);
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
        reset(BUILD_DAO);
    }

    @Nested
    class GetMetrics {

        @Test
        void shouldReturnMetrics() {
            when(BUILD_DAO.countBuildsInSystemInRange(eq(1L), any(Instant.class), any(Instant.class)))
                    .thenReturn(2L)
                    .thenReturn(1L);

            var response = RESOURCES
                    .target("/metrics")
                    .request()
                    .get();

            assertOkResponse(response);

            var result = response.readEntity(new GenericType<Map<String, Map<String, Long>>>() {});

            assertThat(result).isEqualTo(Map.of("builds", Map.of("current", 2L, "previous", 1L)));

            verify(BUILD_DAO, times(2)).countBuildsInSystemInRange(eq(1L), any(Instant.class), any(Instant.class));
            verifyNoMoreInteractions(BUILD_DAO);
        }
    }
}
