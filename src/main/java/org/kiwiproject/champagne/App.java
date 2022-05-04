package org.kiwiproject.champagne;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.kiwiproject.champagne.config.AppConfig;
import org.kiwiproject.champagne.jdbi.UserDao;
import org.kiwiproject.champagne.resource.UserResource;
import org.kiwiproject.dropwizard.jdbi3.Jdbi3Builders;
import org.kiwiproject.dropwizard.util.config.JacksonConfig;
import org.kiwiproject.dropwizard.util.jackson.StandardJacksonConfigurations;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

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
        bootstrap.addBundle(new AssetsBundle("/ui", "/", "index.html"));
    }

    @Override
    public void run(AppConfig configuration, Environment environment) throws Exception {

        setupJsonProcessing(environment);
        
        // Setting the base url pattern for the REST endpoints to /api as the UI will be on the root path
        environment.jersey().setUrlPattern("/api/*");

        var jdbi = Jdbi3Builders.buildManagedJdbi(environment, configuration.getDataSourceFactory(), new PostgresPlugin());

        environment.jersey().register(new UserResource(jdbi.onDemand(UserDao.class)));

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
