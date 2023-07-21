package org.kiwiproject.champagne;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.dhatim.dropwizard.jwt.cookie.authentication.JwtCookieAuthBundle;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.kiwiproject.champagne.config.AppConfig;
import org.kiwiproject.champagne.dao.*;
import org.kiwiproject.champagne.dao.mappers.BuildMapper;
import org.kiwiproject.champagne.job.CleanOutAuditsJob;
import org.kiwiproject.champagne.model.Build;
import org.kiwiproject.champagne.resource.*;
import org.kiwiproject.champagne.resource.filter.DeployableSystemRequestFilter;
import org.kiwiproject.champagne.service.ManualTaskService;
import org.kiwiproject.dropwizard.error.ErrorContextBuilder;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;
import org.kiwiproject.dropwizard.error.model.ServiceDetails;
import org.kiwiproject.dropwizard.error.resource.ApplicationErrorResource;
import org.kiwiproject.dropwizard.jdbi3.Jdbi3Builders;
import org.kiwiproject.dropwizard.util.config.JacksonConfig;
import org.kiwiproject.dropwizard.util.exception.StandardExceptionMappers;
import org.kiwiproject.dropwizard.util.jackson.StandardJacksonConfigurations;
import org.kiwiproject.dropwizard.util.server.DropwizardConnectors;
import org.kiwiproject.json.JsonHelper;
import org.kiwiproject.net.KiwiInternetAddresses;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

import static org.kiwiproject.dropwizard.util.job.MonitoredJobs.registerJob;

public class App extends Application<AppConfig> {

    public static void main(String[] args) throws Exception {
        new App().run(args);    
    }

    @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addBundle(new JdbiExceptionsBundle());
        bootstrap.addBundle(new MigrationsBundle<>() {
            @Override
            public DataSourceFactory getDataSourceFactory(AppConfig config) {
                return config.getDataSourceFactory();
            }
        });

        var authBundle = JwtCookieAuthBundle.<AppConfig>getDefault()
                .withConfigurationSupplier(AppConfig::getJwtCookieAuth);

        bootstrap.addBundle(authBundle);
    }

    @Override
    public void run(AppConfig configuration, Environment environment) {

        setupJsonProcessing(environment);
        StandardExceptionMappers.register(configuration.getServerFactory(), environment);

        var jdbi = Jdbi3Builders.buildManagedJdbi(environment, configuration.getDataSourceFactory(), new PostgresPlugin());
        
        var auditRecordDao = jdbi.onDemand(AuditRecordDao.class);
        var userDao = jdbi.onDemand(UserDao.class);
        var releaseDao = jdbi.onDemand(ReleaseDao.class);
        var releaseStatusDao = jdbi.onDemand(ReleaseStatusDao.class);
        var taskDao = jdbi.onDemand(TaskDao.class);
        var taskStatusDao = jdbi.onDemand(TaskStatusDao.class);
        var deploymentEnvironmentDao = jdbi.onDemand(DeploymentEnvironmentDao.class);
        var buildDao = jdbi.onDemand(BuildDao.class);
        var hostDao = jdbi.onDemand(HostDao.class);
        var componentDao = jdbi.onDemand(ComponentDao.class);
        var errorDao = setupApplicationErrors(jdbi, configuration, environment);
        var deployableSystemDao = jdbi.onDemand(DeployableSystemDao.class);

        var jsonHelper = JsonHelper.newDropwizardJsonHelper();
        jdbi.registerRowMapper(Build.class, new BuildMapper(jsonHelper));

        var manualTaskService = new ManualTaskService(releaseDao, releaseStatusDao, taskDao, taskStatusDao);

        environment.jersey().register(new AuthResource(userDao));
        environment.jersey().register(new AuditRecordResource(auditRecordDao));
        environment.jersey().register(new BuildResource(buildDao, jsonHelper));
        environment.jersey().register(new DeploymentEnvironmentResource(deploymentEnvironmentDao, auditRecordDao, errorDao, manualTaskService));
        environment.jersey().register(new HostConfigurationResource(hostDao, componentDao, auditRecordDao, errorDao));
        environment.jersey().register(new TaskResource(releaseDao, releaseStatusDao, taskDao, taskStatusDao, deploymentEnvironmentDao, auditRecordDao, errorDao));
        environment.jersey().register(new UserResource(userDao, auditRecordDao, errorDao));
        environment.jersey().register(new ApplicationErrorResource(errorDao));
        environment.jersey().register(new DeployableSystemResource(deployableSystemDao, userDao, auditRecordDao, errorDao));

        configureCors(environment);

        // Setup jobs
        var cleanOutAuditsJob = new CleanOutAuditsJob(auditRecordDao, configuration.getAuditRecordsMaxRetain().toMilliseconds());
        registerJob(environment, "Clean Out Audits", configuration.getAuditCleanup(), cleanOutAuditsJob);

        environment.jersey().register(new DeployableSystemRequestFilter(deployableSystemDao, userDao));
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

    private ApplicationErrorDao setupApplicationErrors(Jdbi jdbi, AppConfig configuration, Environment environment) {
        var hostPort = KiwiInternetAddresses.getLocalHostInfo().orElseThrow();
        var serverFactory = DropwizardConnectors.requireDefaultServerFactory(configuration.getServerFactory());
        var port = DropwizardConnectors.getApplicationPort(serverFactory, DropwizardConnectors.ConnectorType.HTTP).orElseThrow();
        var serviceDetails = ServiceDetails.from(hostPort.getHostName(), hostPort.getIpAddr(), port);
        return ErrorContextBuilder.newInstance()
                .environment(environment)
                .serviceDetails(serviceDetails)
                .buildWithJdbi3(jdbi)
                .errorDao();
    }

}
