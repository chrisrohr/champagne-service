package org.kiwiproject.champagne.util;

import lombok.experimental.UtilityClass;
import org.kiwiproject.champagne.model.DeployableSystemThreadLocal;
import org.kiwiproject.jaxrs.exception.JaxrsBadRequestException;

@UtilityClass
public class DeployableSystems {

    public static long getSystemIdOrThrowBadRequest() {
        return DeployableSystemThreadLocal.getCurrentDeployableSystem()
                .orElseThrow(() -> new JaxrsBadRequestException("Missing deployable system"));
    }

    public static Long getSystemIdOrNull() {
        return DeployableSystemThreadLocal.getCurrentDeployableSystem().orElse(null);
    }
}
