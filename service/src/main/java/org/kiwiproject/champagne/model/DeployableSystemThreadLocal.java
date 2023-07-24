package org.kiwiproject.champagne.model;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DeployableSystemThreadLocal {

    @Getter
    @AllArgsConstructor
    public class DeployableSystemInfo {
        Long id;
        boolean admin;
    }

    private static final ThreadLocal<DeployableSystemInfo> THREAD_LOCAL = new ThreadLocal<>();

    public static Optional<DeployableSystemInfo> getCurrentDeployableSystem() {
        return Optional.ofNullable(THREAD_LOCAL.get());
    }

    public static void setCurrentDeployableSystem(Long id, boolean admin) {
        THREAD_LOCAL.set(new DeployableSystemInfo(id, admin));
    }

    public static void clearDeployableSystem() {
        THREAD_LOCAL.remove();
    }

}
