package org.kiwiproject.champagne.core.manualdeployment;

import java.time.Instant;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Value;

/**
 * Defines the status of a specific manual task in a specific deployment environment.
 */
@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskStatus {

    Long id;
    Instant createdAt;
    Instant updatedAt;

    @NotNull
    Long taskId;

    @NotNull
    Long environmentId;

    @NotNull
    DeploymentTaskStatus status;
    
}
