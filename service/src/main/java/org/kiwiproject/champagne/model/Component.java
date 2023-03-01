package org.kiwiproject.champagne.model;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

/**
 * Representation of a deployable component in the system.
 */
@Builder
@Value
public class Component {
    
    Long id;
    Instant createdAt;
    Instant updatedAt;
    
    /**
     * The name of the component
     */
    String componentName;

    /**
     * A tag for the component used to link up with {@link Host} instances for deployments
     */
    String tag;

    /**
     * The Deployable System that this component is tied to
     */
    Long deployableSystemId;

}
