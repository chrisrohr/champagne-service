package org.kiwiproject.champagne.resource.apps;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.dhatim.dropwizard.jwt.cookie.authentication.JwtCookieAuthBundle;
import org.kiwiproject.champagne.config.AppConfig;
import org.kiwiproject.champagne.dao.AuditRecordDao;
import org.kiwiproject.champagne.dao.UserDao;
import org.kiwiproject.champagne.resource.AuthResource;
import org.kiwiproject.champagne.resource.UserResource;

public class TestUserApp extends Application<AppConfig> {

    public static UserDao userDao;
    public static AuditRecordDao auditRecordDao;

    @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {
        bootstrap.addBundle(JwtCookieAuthBundle.<AppConfig>getDefault()
                .withConfigurationSupplier(AppConfig::getJwtCookieAuth));
    }

    @Override
    public void run(AppConfig appConfig, Environment environment) {
        environment.jersey().register(new UserResource(userDao, auditRecordDao));
        environment.jersey().register(new AuthResource(userDao));
    }
}
