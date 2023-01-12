package org.kiwiproject.champagne.auth;

import java.util.Optional;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityThreadLocal {
    
    private static final ThreadLocal<UserPrincipal> SECURITY_DATA_THREAD_LOCAL = new ThreadLocal<>();

    public static Optional<UserPrincipal> getSecurityData() {
        return Optional.ofNullable(SECURITY_DATA_THREAD_LOCAL.get());
    }

    public static UserPrincipal setSecurityData(UserPrincipal user) {
        SECURITY_DATA_THREAD_LOCAL.set(user);
        return user;
    }

    public static void clearSecurityData() {
        SECURITY_DATA_THREAD_LOCAL.remove();
    }
}
