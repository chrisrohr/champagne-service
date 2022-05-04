package org.kiwiproject.champagne.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppConfig extends Configuration {
    
    @NotNull
    @Valid
    @JsonProperty("database")
    private DataSourceFactory dataSourceFactory = new DataSourceFactory();
}
