package org.kiwiproject.champagne.core.manualdeployment;

import java.time.Instant;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Value;

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
}
