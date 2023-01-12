package org.kiwiproject.champagne.auth;

import static com.codahale.metrics.MetricRegistry.name;
import static java.util.Objects.nonNull;

import java.security.Principal;
import java.util.Optional;
import java.util.AbstractMap.SimpleEntry;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

public class CachingJwtAuthenticator<P extends Principal> implements Authenticator<JwtContext, P> {

    private final Authenticator<JwtContext, P> authenticator;
    private final Cache<String, SimpleEntry<JwtContext, Optional<P>>> cache;
    private final Meter cacheMisses;
    private final Timer gets;

    public CachingJwtAuthenticator(MetricRegistry metricRegistry,
                                   Authenticator<JwtContext, P> authenticator,
                                   CacheBuilderSpec cacheSpec) {
        this(metricRegistry, authenticator, CacheBuilder.from(cacheSpec));
    }

    public CachingJwtAuthenticator(MetricRegistry metricRegistry,
                                   Authenticator<JwtContext, P> authenticator,
                                   CacheBuilder<Object, Object> builder) {

        this.authenticator = authenticator;
        this.cacheMisses = metricRegistry.meter(name(authenticator.getClass(), "cache-misses"));
        this.gets = metricRegistry.timer(name(authenticator.getClass(), "gets"));
        this.cache = builder.recordStats().build();
    }

    @Override
    public Optional<P> authenticate(JwtContext context) throws AuthenticationException {
        var timer = gets.time();

        try {
            var cacheEntry = cache.getIfPresent(context.getToken());
            if (nonNull(cacheEntry)) {
                return cacheEntry.getValue();
            }

            cacheMisses.mark();

            var principal = authenticator.authenticate(context);
            if (principal.isPresent()) {
                cache.put(context.getToken(), new SimpleEntry<>(context, principal));
            }
            
            return principal;
        } finally {
            timer.stop();
        }
    }


}
