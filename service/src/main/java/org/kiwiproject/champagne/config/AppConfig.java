package org.kiwiproject.champagne.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.dhatim.dropwizard.jwt.cookie.authentication.JwtCookieAuthConfiguration;
import org.kiwiproject.dropwizard.util.config.JobSchedule;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AppConfig extends Configuration {
    
    @NotNull
    @Valid
    @JsonProperty("database")
    private DataSourceFactory dataSourceFactory = new DataSourceFactory();

    @NotBlank
    private String jwtCacheSpec = "expireAfterWrite=60m";

    @NotNull
    @Valid
    private JwtCookieAuthConfiguration jwtCookieAuth = new JwtCookieAuthConfiguration();

    @NotNull
    private JobSchedule auditCleanup = JobSchedule.ofIntervalDelay(Duration.days(1));

    @NotNull
    private Duration auditRecordsMaxRetain = Duration.days(30);
}
