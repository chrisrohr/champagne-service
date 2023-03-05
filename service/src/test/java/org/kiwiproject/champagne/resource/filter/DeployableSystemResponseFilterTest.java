package org.kiwiproject.champagne.resource.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

@DisplayName("DeployableSystemResponseFilter")
class DeployableSystemResponseFilterTest {

    @Test
    void shouldClearDeployableSystem() {
        DeployableSystemThreadLocal.setCurrentDeployableSystem(1L);
        var filter = new DeployableSystemResponseFilter();

        filter.filter(mock(ContainerRequestContext.class), mock(ContainerResponseContext.class));

        assertThat(DeployableSystemThreadLocal.getCurrentDeployableSystem()).isEmpty();
    }
}
