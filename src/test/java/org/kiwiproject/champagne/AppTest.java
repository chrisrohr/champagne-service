package org.kiwiproject.champagne;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.test.dropwizard.app.DropwizardAppTests.healthCheckNamesOf;
import static org.kiwiproject.test.dropwizard.app.DropwizardAppTests.registeredResourceClassesOf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.champagne.config.AppConfig;
import org.kiwiproject.champagne.resource.UserResource;
import org.kiwiproject.test.dropwizard.app.PostgresAppTestExtension;

import io.dropwizard.testing.junit5.DropwizardAppExtension;

class AppTest {

    @RegisterExtension
    static final PostgresAppTestExtension<AppConfig> POSTGRES_APP_TEST_EXTENSION =
        new PostgresAppTestExtension<>("migrations.xml", "config-unit-test.yml", App.class);

    private static final DropwizardAppExtension<AppConfig> APP = POSTGRES_APP_TEST_EXTENSION.getApp();

    @Test
    void shouldRegisterResources() {
        assertThat(registeredResourceClassesOf(APP)).contains(
            UserResource.class
        );
    }

    @Test
    void shouldRegisterHealthChecks() {
        assertThat(healthCheckNamesOf(APP)).contains("deadlocks");
    }
}
