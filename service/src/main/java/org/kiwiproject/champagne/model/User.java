package org.kiwiproject.champagne.model;

import java.time.Instant;

import javax.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Value;

/**
 * Core model for user information.
 * 
 * @implNote This model is soft-deletable because we will be linking to users to track auditable events (created/updated) and if the user is hard deleted
 *           then we will lose the pedigree of the changes.
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
}
