package org.kiwiproject.champagne.resource.apps;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.dhatim.dropwizard.jwt.cookie.authentication.JwtCookieAuthBundle;
import org.kiwiproject.champagne.config.AppConfig;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.DeployableSystemDao;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.champagne.resource.AuthResource;
import org.kiwiproject.champagne.resource.UserResource;
import org.kiwiproject.dropwizard.error.dao.ApplicationErrorDao;

public class TestUserApp extends Application<AppConfig> {

    public static UserDao userDao;
    public static DeployableSystemDao deployableSystemDao;
    public static AuditRecordDao auditRecordDao;
    public static ApplicationErrorDao errorDao;

    @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {
        bootstrap.addBundle(JwtCookieAuthBundle.<AppConfig>getDefault()
                .withConfigurationSupplier(AppConfig::getJwtCookieAuth));
    }

    @Override
    public void run(AppConfig appConfig, Environment environment) {
        environment.jersey().register(new UserResource(userDao, deployableSystemDao, auditRecordDao, errorDao));
        environment.jersey().register(new AuthResource(userDao));
    }
}
