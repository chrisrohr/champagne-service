package org.kiwiproject.champagne.model.manualdeployment;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * The top-level container for all manual deployment tasks for a specific release.
 */
@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Release {
    
    Long id;

    Instant createdAt;
    Instant updatedAt;

    @NotBlank
    String releaseNumber;

    /**
     * The Deployable System that this release is tied to
     */
    @With
    Long deployableSystemId;
}
