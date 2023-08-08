package org.kiwiproject.champagne.resource.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;

public class DeployableSystemResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        DeployableSystemThreadLocal.clearDeployableSystem();
    }
}
