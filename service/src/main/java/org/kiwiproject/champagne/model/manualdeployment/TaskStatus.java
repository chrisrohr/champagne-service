package org.kiwiproject.champagne.model.manualdeployment;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
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
