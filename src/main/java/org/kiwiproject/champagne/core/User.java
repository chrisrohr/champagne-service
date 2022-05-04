package org.kiwiproject.champagne.core;

import java.time.Instant;

import javax.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class User {
    
    long id;
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
    
}
