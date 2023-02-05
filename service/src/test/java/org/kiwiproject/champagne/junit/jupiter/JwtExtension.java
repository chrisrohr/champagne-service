package org.kiwiproject.champagne.junit.jupiter;

import org.dhatim.dropwizard.jwt.cookie.authentication.CurrentPrincipal;
import org.dhatim.dropwizard.jwt.cookie.authentication.DefaultJwtCookiePrincipal;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.kiwiproject.reflect.RuntimeReflectionException;

import java.security.Principal;

public class JwtExtension implements BeforeEachCallback, AfterEachCallback {

    private final String username;

    public JwtExtension(String username) {
        this.username = username;
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        try {
            var method = CurrentPrincipal.class.getDeclaredMethod("remove");
            method.setAccessible(true);

            method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeReflectionException("Unable to remove user", e);
        }
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        var principal = new DefaultJwtCookiePrincipal(username);

        try {
            var method = CurrentPrincipal.class.getDeclaredMethod("set", Principal.class);
            method.setAccessible(true);

            method.invoke(null, principal);
        } catch (Exception e) {
            throw new RuntimeReflectionException("Unable to set user", e);
        }
    }
}
