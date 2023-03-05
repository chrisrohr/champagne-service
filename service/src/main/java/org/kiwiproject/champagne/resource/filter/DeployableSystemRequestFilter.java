package org.kiwiproject.champagne.resource.filter;

import org.apache.commons.lang3.StringUtils;
import org.kiwiproject.champagne.dao.DeployableSystemDao;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

public class DeployableSystemRequestFilter implements ContainerRequestFilter {

    private static final String DEPLOYABLE_SYSTEM_HEADER_NAME = "Champagne-Deployable-System";

    private final DeployableSystemDao deployableSystemDao;

    public DeployableSystemRequestFilter(DeployableSystemDao deployableSystemDao) {
        this.deployableSystemDao = deployableSystemDao;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var system = requestContext.getHeaderString(DEPLOYABLE_SYSTEM_HEADER_NAME);

        if (StringUtils.isNotBlank(system)) {
            var userName = requestContext.getSecurityContext().getUserPrincipal().getName();

            if (deployableSystemDao.isUserBySystemIdentifierInSystem(userName, Long.parseLong(system))) {
                DeployableSystemThreadLocal.setCurrentDeployableSystem(Long.valueOf(system));
            }
        }
    }
    
}
