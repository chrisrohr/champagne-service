package org.kiwiproject.champagne.model.manualdeployment;

import java.time.Instant;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Value;

/**
 * Defines one specific manual task needed when deploying a specific release.
 */
@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {

    Long id;
    Instant createdAt;
    Instant updatedAt;

    @NotNull
    Long releaseId;

    @NotNull
    ReleaseStage stage;

    @NotBlank
    String summary;

    String description;

    @NotBlank
    String component;
    
}
