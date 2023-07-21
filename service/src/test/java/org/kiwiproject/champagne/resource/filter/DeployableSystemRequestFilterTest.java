package org.kiwiproject.champagne.resource.filter;

import org.dhatim.dropwizard.jwt.cookie.authentication.DefaultJwtCookiePrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.champagne.dao.DeployableSystemDao;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal.DeployableSystemInfo;
import org.kiwiproject.champagne.model.User;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("DeployableSystemRequestFilter")
class DeployableSystemRequestFilterTest {

    @BeforeEach
    void setUp() {
        DeployableSystemThreadLocal.clearDeployableSystem();
    }

    @Test
    void shouldNotSetDeployableSystemWhenHeaderNotSet() {
        var dao = mock(DeployableSystemDao.class);
        var userDao = mock(UserDao.class);
        var filter = new DeployableSystemRequestFilter(dao, userDao);

        var context = mock(ContainerRequestContext.class);
        when(context.getHeaderString("Champagne-Deployable-System")).thenReturn(null);

        filter.filter(context);

        assertThat(DeployableSystemThreadLocal.getCurrentDeployableSystem()).isEmpty();
    }

    @Test
    void shouldNotSetDeployableSystemWhenHeaderSetButUserNotInSystem() {
        var dao = mock(DeployableSystemDao.class);
        var userDao = mock(UserDao.class);
        var filter = new DeployableSystemRequestFilter(dao, userDao);

        var context = mock(ContainerRequestContext.class);
        when(context.getHeaderString("Champagne-Deployable-System")).thenReturn("1");

        var securityContext = mock(SecurityContext.class);
        when(context.getSecurityContext()).thenReturn(securityContext);

        var principal = new DefaultJwtCookiePrincipal("bob");
        when(securityContext.getUserPrincipal()).thenReturn(principal);

        when(userDao.findBySystemIdentifier("bob")).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(dao.isUserInSystem(1L, 1L)).thenReturn(false);

        filter.filter(context);

        assertThat(DeployableSystemThreadLocal.getCurrentDeployableSystem()).isEmpty();
    }

    @Test
    void shouldSetDeployableSystemWhenHeaderSetAndUserInSystem() {
        var dao = mock(DeployableSystemDao.class);
        var userDao = mock(UserDao.class);
        var filter = new DeployableSystemRequestFilter(dao, userDao);

        var context = mock(ContainerRequestContext.class);
        when(context.getHeaderString("Champagne-Deployable-System")).thenReturn("1");

        var securityContext = mock(SecurityContext.class);
        when(context.getSecurityContext()).thenReturn(securityContext);

        var principal = new DefaultJwtCookiePrincipal("bob");
        when(securityContext.getUserPrincipal()).thenReturn(principal);

        when(userDao.findBySystemIdentifier("bob")).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(dao.isUserInSystem(1L, 1L)).thenReturn(true);
        when(dao.isUserAdminOfSystem(1L, 1L)).thenReturn(true);

        filter.filter(context);

        assertThat(DeployableSystemThreadLocal.getCurrentDeployableSystem().map(DeployableSystemInfo::getId)).contains(1L);
    }
}
