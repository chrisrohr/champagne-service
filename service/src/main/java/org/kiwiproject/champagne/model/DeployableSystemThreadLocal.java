package org.kiwiproject.champagne.model;

import java.util.Optional;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DeployableSystemThreadLocal {

    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    public static Optional<Long> getCurrentDeployableSystem() {
        return Optional.ofNullable(THREAD_LOCAL.get());
    }

    public static void setCurrentDeployableSystem(Long id) {
        THREAD_LOCAL.set(id);
    }

    public static void clearCurrentDeployableSystem() {
        THREAD_LOCAL.remove();
    }
    
}
