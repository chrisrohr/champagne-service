package org.kiwiproject.champagne.resource.filter;

import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

public class DeployableSystemResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        DeployableSystemThreadLocal.clearDeployableSystem();
    }
}
