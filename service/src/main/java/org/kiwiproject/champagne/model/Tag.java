package org.kiwiproject.champagne.model;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Representation of a tag to link Hosts and Components.
 */
@Builder
@Value
public class Tag {

    Long id;
    Instant createdAt;
    Instant updatedAt;

    @NotBlank
    String name;

    @With
    Long deployableSystemId;
}
