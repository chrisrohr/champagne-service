package org.kiwiproject.champagne.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;
import org.kiwiproject.jaxrs.exception.JaxrsBadRequestException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DeployableSystems")
class DeployableSystemsTest {

    @AfterEach
    void tearDown() {
        DeployableSystemThreadLocal.clearDeployableSystem();
    }

    @Nested
    class GetSystemIdOrThrowBadRequest {

        @Test
        void shouldReturnSystemIdWhenFound() {
            DeployableSystemThreadLocal.setCurrentDeployableSystem(1L, false);

            assertThat(DeployableSystems.getSystemIdOrThrowBadRequest()).isEqualTo(1L);
        }

        @Test
        void shouldThrowJaxrsBadRequestWhenNotFound() {
            assertThatThrownBy(DeployableSystems::getSystemIdOrThrowBadRequest)
                    .isInstanceOf(JaxrsBadRequestException.class)
                    .hasMessage("Missing deployable system");
        }
    }

    @Nested
    class GetSystemIdOrNull {

        @Test
        void shouldReturnSystemIdWhenFound() {
            DeployableSystemThreadLocal.setCurrentDeployableSystem(1L, false);

            assertThat(DeployableSystems.getSystemIdOrNull()).isEqualTo(1L);
        }

        @Test
        void shouldReturnNullWhenNotFound() {
            assertThat(DeployableSystems.getSystemIdOrNull()).isNull();
        }
    }
}
