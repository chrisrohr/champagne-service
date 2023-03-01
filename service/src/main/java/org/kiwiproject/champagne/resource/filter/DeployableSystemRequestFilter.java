package org.kiwiproject.champagne.resource.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.apache.commons.lang3.StringUtils;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;

public class DeployableSystemRequestFilter implements ContainerRequestFilter {

    private static final String DEPLOYABLE_SYSTEM_HEADER_NAME = "DeployableSystem";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        var system = requestContext.getHeaderString(DEPLOYABLE_SYSTEM_HEADER_NAME);

        // TODO: Need to validate that user is in system

        if (StringUtils.isNotBlank(system)) {
            DeployableSystemThreadLocal.setCurrentDeployableSystem(Long.valueOf(system));
        }
    }
    
}
