package org.kiwiproject.champagne.core.manualdeployment;

import java.time.Instant;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Value;

/**
 * Defines the status of manual tasks for a release in a specific deployment environment.
 */
@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReleaseStatus {
    
    Long id;
    Instant createdAt;
    Instant updatedAt;

    @NotNull
    Long releaseId;

    @NotNull
    Long environmentId;

    @NotNull
    DeploymentTaskStatus status;
}
