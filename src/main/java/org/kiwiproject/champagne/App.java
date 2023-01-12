package org.kiwiproject.champagne;

import java.security.Principal;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.kiwiproject.champagne.auth.CachingJwtAuthenticator;
import org.kiwiproject.champagne.auth.JwtAuthFilter;
import org.kiwiproject.champagne.auth.JwtAuthenticator;
import org.kiwiproject.champagne.auth.JwtProcessor;
import org.kiwiproject.champagne.auth.UserPrincipal;
import org.kiwiproject.champagne.config.AppConfig;
import org.kiwiproject.champagne.jdbi.UserDao;
import org.kiwiproject.champagne.resource.UserResource;
import org.kiwiproject.dropwizard.jdbi3.Jdbi3Builders;
import org.kiwiproject.dropwizard.util.config.JacksonConfig;
import org.kiwiproject.dropwizard.util.jackson.StandardJacksonConfigurations;

import com.google.common.cache.CacheBuilderSpec;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class App extends Application<AppConfig> {

    public static void main(String[] args) throws Exception {
        new App().run(args);    
    }

    @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {
        bootstrap.addBundle(new JdbiExceptionsBundle());
        bootstrap.addBundle(new MigrationsBundle<>() {
            @Override
            public DataSourceFactory getDataSourceFactory(AppConfig config) {
                return config.getDataSourceFactory();
            }
        });
    }

    @Override
    public void run(AppConfig configuration, Environment environment) throws Exception {

        setupJsonProcessing(environment);

        var jdbi = Jdbi3Builders.buildManagedJdbi(environment, configuration.getDataSourceFactory(), new PostgresPlugin());
        var userDao = jdbi.onDemand(UserDao.class);

        setupAuth(configuration, environment, userDao);

        environment.jersey().register(new UserResource(userDao));

        configureCors(environment);
    }

    private static void setupJsonProcessing(Environment environment) {
        var jacksonConfig = JacksonConfig.builder()
            .ignoreButWarnForUnknownJsonProperties(true)
            .registerHealthCheckForUnknownJsonProperties(true)
            .readAndWriteDateTimestampsAsMillis(true)
            .writeNilJaxbElementsAsNull(true)
            .build();

            StandardJacksonConfigurations.registerAllStandardJacksonConfigurations(jacksonConfig, environment);
    }

    private static void setupAuth(AppConfig configuration, Environment environment, UserDao userDao) {
        var key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

        var processor = new JwtProcessor(key);

        var authenticator = JwtAuthenticator.builder().userDao(userDao).build();
        var cachingAuthenticator = new CachingJwtAuthenticator<UserPrincipal>(environment.metrics(), authenticator, 
                    CacheBuilderSpec.parse(configuration.getJwtCacheSpec()));

        var authFilter = new JwtAuthFilter.Builder<UserPrincipal>()
            .setProcessor(processor)
            .setRealm("realm")
            .setPrefix("Bearer")
            .setAuthenticator(cachingAuthenticator)
            .buildAuthFilter();

        environment.jersey().register(new AuthDynamicFeature(authFilter));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(Principal.class));

        // TODO: When we add the admin role we can register the RolesAllowedDynamicFeature from jersey
        //       that will allow us to annotate resource methods with required roles.

    }

    private static void configureCors(Environment environment) {
        FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS params
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Authorization");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,PUT,POST,DELETE,HEAD");
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }

}
