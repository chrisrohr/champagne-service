package org.kiwiproject.champagne.resource;

import static jakarta.ws.rs.client.Entity.json;
import static org.kiwiproject.test.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.OptionalInt;
import java.util.OptionalLong;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.junit.jupiter.DeployableSystemExtension;
import org.kiwiproject.champagne.junit.jupiter.JwtExtension;
import org.kiwiproject.dropwizard.error.resource.ApplicationErrorResource;
import org.kiwiproject.dropwizard.util.exception.JerseyViolationExceptionMapper;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;

@DisplayName("BuildResource")
@ExtendWith({DropwizardExtensionsSupport.class, DeployableSystemExtension.class})
class ApplicationErrorWithAuthResourceTest {

    private static final ApplicationErrorResource DELEGATE = mock(ApplicationErrorResource.class);

    private static final ApplicationErrorWithAuthResource RESOURCE = new ApplicationErrorWithAuthResource(DELEGATE);

    private static final ResourceExtension RESOURCES = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(RESOURCE)
            .addProvider(JerseyViolationExceptionMapper.class)
            .addProvider(JaxrsExceptionMapper.class)
            .build();

    @RegisterExtension
    public final JwtExtension jwtExtension = new JwtExtension("bob");

    @AfterEach
    void cleanup() {
        reset(DELEGATE);
    }

    @Nested
    class GetById {

        @Test
        void delegateToGetById() {
            when(DELEGATE.getById(OptionalLong.of(1L))).thenReturn(Response.ok().build());

            var response = RESOURCES.client()
                    .target("/errors/{id}")
                    .resolveTemplate("id", 1L)
                    .request()
                    .get();

            assertOkResponse(response);

            verify(DELEGATE).getById(OptionalLong.of(1L));
        }
    }

    @Nested
    class GetErrors {

        @Test
        void shouldDelegateToGetErrors() {
            when(DELEGATE.getErrors(anyString(), any(OptionalInt.class), any(OptionalInt.class))).thenReturn(Response.ok().build());

            var response = RESOURCES.client()
                    .target("/errors")
                    .request()
                    .get();

            assertOkResponse(response);

            verify(DELEGATE).getErrors(anyString(), any(OptionalInt.class), any(OptionalInt.class));
        }
    }

    @Nested
    class Resolve {

        @Test
        void shouldDelegateToResolve() {
            when(DELEGATE.resolve(OptionalLong.of(1L))).thenReturn(Response.ok().build());

            var response = RESOURCES.client()
                    .target("/errors/resolve/{id}")
                    .resolveTemplate("id", 1L)
                    .request()
                    .put(json(""));

            assertOkResponse(response);

            verify(DELEGATE).resolve(OptionalLong.of(1L));
        }
    }

    @Nested
    class ResolveAllUnresolved {

        @Test
        void shouldDelegateToResolveAllUnresolved() {
            when(DELEGATE.resolveAllUnresolved()).thenReturn(Response.ok().build());

            var response = RESOURCES.client()
                    .target("/errors/resolve")
                    .request()
                    .put(json(""));

            assertOkResponse(response);

            verify(DELEGATE).resolveAllUnresolved();
        }
    }
}
