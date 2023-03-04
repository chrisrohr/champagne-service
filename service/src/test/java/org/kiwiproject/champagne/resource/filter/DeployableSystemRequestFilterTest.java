package org.kiwiproject.champagne.resource.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dhatim.dropwizard.jwt.cookie.authentication.DefaultJwtCookiePrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.champagne.dao.DeployableSystemDao;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

@DisplayName("DeployableSystemRequestFilter")
class DeployableSystemRequestFilterTest {

    @BeforeEach
    void setUp() {
        DeployableSystemThreadLocal.clearDeployableSystem();
    }

    @Test
    void shouldNotSetDeployableSystemWhenHeaderNotSet() {
        var dao = mock(DeployableSystemDao.class);
        var filter = new DeployableSystemRequestFilter(dao);

        var context = mock(ContainerRequestContext.class);
        when(context.getHeaderString("DeployableSystem")).thenReturn(null);

        filter.filter(context);

        assertThat(DeployableSystemThreadLocal.getCurrentDeployableSystem()).isEmpty();
    }

    @Test
    void shouldNotSetDeployableSystemWhenHeaderSetButUserNotInSystem() {
        var dao = mock(DeployableSystemDao.class);
        var filter = new DeployableSystemRequestFilter(dao);

        var context = mock(ContainerRequestContext.class);
        when(context.getHeaderString("DeployableSystem")).thenReturn("1");

        var securityContext = mock(SecurityContext.class);
        when(context.getSecurityContext()).thenReturn(securityContext);

        var principal = new DefaultJwtCookiePrincipal("bob");
        when(securityContext.getUserPrincipal()).thenReturn(principal);

        when(dao.isUserBySystemIdentifierInSystem("bob", 1)).thenReturn(false);

        filter.filter(context);

        assertThat(DeployableSystemThreadLocal.getCurrentDeployableSystem()).isEmpty();
    }

    @Test
    void shouldSetDeployableSystemWhenHeaderSetAndUserInSystem() {
        var dao = mock(DeployableSystemDao.class);
        var filter = new DeployableSystemRequestFilter(dao);

        var context = mock(ContainerRequestContext.class);
        when(context.getHeaderString("DeployableSystem")).thenReturn("1");

        var securityContext = mock(SecurityContext.class);
        when(context.getSecurityContext()).thenReturn(securityContext);

        var principal = new DefaultJwtCookiePrincipal("bob");
        when(securityContext.getUserPrincipal()).thenReturn(principal);

        when(dao.isUserBySystemIdentifierInSystem("bob", 1)).thenReturn(true);

        filter.filter(context);

        assertThat(DeployableSystemThreadLocal.getCurrentDeployableSystem()).contains(1L);
    }
}
