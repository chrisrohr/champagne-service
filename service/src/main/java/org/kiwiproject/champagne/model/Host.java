package org.kiwiproject.champagne.model;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Value;

/**
 * Representation of a host used to deploy components. A Host could be created and stored in the database or 
 * dynamically pulls from a cloud provider like AWS.
 */
@Builder
@Value
public class Host {

    public enum Source {
        CHAMPAGNE, AWS
    }
    
    Long id;
    Instant createdAt;
    Instant updatedAt;

    /**
     * The id of the {@link DeploymentEnvironment} that this host lives in.
     */
    Long environmentId;

    /**
     * The hostname of the host.
     */
    String hostname;

    /**
     * List of tags used to link up components to hosts dynamically.
     */
    List<String> tags;

    /**
     * The source of truth for the definition of this host, whether it is stored in Champagne or pulled dynamically from a cloud.
     */
    Source source;

    /**
     * The Deployable System that this host is tied to
     */
    Long deployableSystemId;
}
