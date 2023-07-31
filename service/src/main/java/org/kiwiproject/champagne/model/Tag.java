package org.kiwiproject.champagne.model;

import javax.validation.constraints.NotBlank;
import java.time.Instant;

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
