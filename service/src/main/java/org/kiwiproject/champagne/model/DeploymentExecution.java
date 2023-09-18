package org.kiwiproject.champagne.model;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Representation of a run of deployments
 */
@Builder
@Value
public class DeploymentExecution {

    public enum Status {
        RUNNING, COMPLETE, FAILED
    }

    Long id;
    Instant createdAt;
    Instant updatedAt;

    Status status;
    String failureOutput;
    Long environmentId;
    String consolidatedChangeLog;
    double percentComplete;

    /**
     * The Deployable System that this component is tied to
     */
    @With
    Long deployableSystemId;

    /**
     * Transient property to represent the fully populated deployment environment.
     */
    @With
    DeploymentEnvironment environment;

    /**
     * Transient property to represent a list of fully populated deployments attached to this execution.
     */
    @With
    List<Deployment> deployments;
}
