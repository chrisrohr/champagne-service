package org.kiwiproject.champagne.util;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.testing.junit5.ResourceExtension;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.experimental.UtilityClass;
import org.dhatim.dropwizard.jwt.cookie.authentication.DefaultJwtCookiePrincipal;
import org.dhatim.dropwizard.jwt.cookie.authentication.DontRefreshSessionFilter;
import org.dhatim.dropwizard.jwt.cookie.authentication.JwtCookieAuthBundle;
import org.dhatim.dropwizard.jwt.cookie.authentication.JwtCookieAuthConfiguration;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.kiwiproject.champagne.config.AppConfig;
import org.kiwiproject.dropwizard.util.exception.JerseyViolationExceptionMapper;
import org.kiwiproject.jaxrs.exception.JaxrsExceptionMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import javax.crypto.SecretKey;

@UtilityClass
public class JwtResourceHelper {

    private static final JwtCookieAuthBundle<AppConfig, DefaultJwtCookiePrincipal> BUNDLE =
            new JwtCookieAuthBundle<>(DefaultJwtCookiePrincipal.class, DefaultJwtCookiePrincipal::getClaims,
                    DefaultJwtCookiePrincipal::new);

    private static final SecretKey KEY = JwtCookieAuthBundle.generateKey("secretSauce");

    public static ResourceExtension configureJwtResource(Object resource) {
        return ResourceExtension.builder()
                .bootstrapLogging(false)
                .addResource(resource)
                .addProvider(JerseyViolationExceptionMapper.class)
                .addProvider(JaxrsExceptionMapper.class)
                .addProvider(RolesAllowedDynamicFeature.class)
                .addProvider(BUNDLE.getAuthRequestFilter(KEY))
                .addProvider(new AuthValueFactoryProvider.Binder<>(DefaultJwtCookiePrincipal.class))
                .addProvider(DontRefreshSessionFilter.class)
                .addProvider(BUNDLE.getAuthResponseFilter(KEY, new JwtCookieAuthConfiguration()))
                .build();
    }

    public static String generateJwt(boolean isAdmin) {
        List<String> roles = isAdmin ? List.of("admin") : List.of();
        var principal = new DefaultJwtCookiePrincipal("bob", false, roles, null);

        Function<DefaultJwtCookiePrincipal, Claims> serializer = DefaultJwtCookiePrincipal::getClaims;
        return Jwts.builder()
                .signWith(KEY, Jwts.SIG.HS256)
                .claims().add(serializer.apply(principal)).and()
                .expiration(Date.from(Instant.now().plus(1800, ChronoUnit.SECONDS)))
                .compact();
    }
}
