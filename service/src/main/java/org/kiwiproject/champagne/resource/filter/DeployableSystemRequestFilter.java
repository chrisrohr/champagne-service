package org.kiwiproject.champagne.resource.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.kiwiproject.champagne.dao.DeployableSystemDao;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;
import org.kiwiproject.jaxrs.exception.JaxrsBadRequestException;

public class DeployableSystemRequestFilter implements ContainerRequestFilter {

    private static final String DEPLOYABLE_SYSTEM_HEADER_NAME = "Champagne-Deployable-System";

    private final DeployableSystemDao deployableSystemDao;
    private final UserDao userDao;

    public DeployableSystemRequestFilter(DeployableSystemDao deployableSystemDao, UserDao userDao) {
        this.deployableSystemDao = deployableSystemDao;
        this.userDao = userDao;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var system = requestContext.getHeaderString(DEPLOYABLE_SYSTEM_HEADER_NAME);

        if (StringUtils.isNotBlank(system)) {
            var userName = requestContext.getSecurityContext().getUserPrincipal().getName();
            var user = userDao.findBySystemIdentifier(userName).orElseThrow(() -> new JaxrsBadRequestException("No user context"));
            var systemId = Long.parseLong(system);

            if (BooleanUtils.isTrue(deployableSystemDao.isUserInSystem(user.getId(), systemId))) {
                var isAdminOfSystem = deployableSystemDao.isUserAdminOfSystem(user.getId(), systemId);
                DeployableSystemThreadLocal.setCurrentDeployableSystem(systemId, isAdminOfSystem);
            }
        }
    }
    
}
