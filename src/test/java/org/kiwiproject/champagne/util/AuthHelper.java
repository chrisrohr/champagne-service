package org.kiwiproject.champagne.util;

import java.security.Principal;

import org.dhatim.dropwizard.jwt.cookie.authentication.CurrentPrincipal;
import org.dhatim.dropwizard.jwt.cookie.authentication.DefaultJwtCookiePrincipal;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthHelper {
    
    public static void setupCurrentPrincipalFor(String username) {
        var principal = new DefaultJwtCookiePrincipal(username);

        try {
            var method = CurrentPrincipal.class.getDeclaredMethod("set", Principal.class);
            method.setAccessible(true);

            method.invoke(null, principal);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set user", e);
        }
    }

    public static void removePrincipal() {
        try {
            var method = CurrentPrincipal.class.getDeclaredMethod("remove");
            method.setAccessible(true);

            method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Unable to remove user", e);
        }
    }
}
