package org.kiwiproject.champagne.util;

import lombok.experimental.UtilityClass;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal.DeployableSystemInfo;
import org.kiwiproject.jaxrs.exception.JaxrsBadRequestException;
import org.kiwiproject.jaxrs.exception.JaxrsNotAuthorizedException;

@UtilityClass
public class DeployableSystems {

    public static long getSystemIdOrThrowBadRequest() {
        var info = DeployableSystemThreadLocal.getCurrentDeployableSystem()
                .orElseThrow(() -> new JaxrsBadRequestException("Missing deployable system"));

        return info.getId();
    }

    public static Long getSystemIdOrNull() {
        return DeployableSystemThreadLocal.getCurrentDeployableSystem().map(DeployableSystemInfo::getId).orElse(null);
    }

    public static void checkUserAdminOfSystem() {
        var info = DeployableSystemThreadLocal.getCurrentDeployableSystem()
                .orElseThrow(() -> new JaxrsBadRequestException("Missing deployable system"));

        if (!info.isAdmin()) {
            throw new JaxrsNotAuthorizedException("User is not authorized to perform this function");
        }
    }
}
