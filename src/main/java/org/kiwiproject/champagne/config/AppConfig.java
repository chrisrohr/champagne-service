package org.kiwiproject.champagne.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import lombok.Getter;
import lombok.Setter;
import org.dhatim.dropwizard.jwt.cookie.authentication.JwtCookieAuthConfiguration;

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
}
