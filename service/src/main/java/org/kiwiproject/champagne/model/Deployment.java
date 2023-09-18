package org.kiwiproject.champagne.model;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Representation of a single deployment
 */
@Builder
@Value
public class Deployment {

    public enum Status {
        STAGED, RUNNING, COMPLETE, IGNORED, MANUAL, FAILED
    }

    Long id;
    Instant createdAt;
    Instant updatedAt;

    Long buildId;
    Long promotionId;
    Instant deployedAt;
    Status status;
    Long environmentId;
    Long executionId;
    String failureOutput;

    /**
     * The Deployable System that this component is tied to
     */
    @With
    Long deployableSystemId;

    /**
     * This is a transient property to store the resolved deployment environment.
     */
    @With
    DeploymentEnvironment environment;

    @With
    Build build;

    @With
    DeploymentExecution execution;

}
