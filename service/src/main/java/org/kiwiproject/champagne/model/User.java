package org.kiwiproject.champagne.model;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Core model for user information.
 */
@Value
@Builder
public class User {
    
    Long id;
    Instant createdAt;
    Instant updatedAt;
    
    @NotBlank
    String firstName;

    @NotBlank
    String lastName;

    @NotBlank
    String displayName;

    @NotBlank
    String systemIdentifier;

    boolean admin;

    @With
    List<DeployableSystem> systems;
}
