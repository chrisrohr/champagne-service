package org.kiwiproject.champagne.core;

import java.time.Instant;

import javax.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DeploymentEnvironment {

    Long id;

    @NotBlank
    String name;

    long createdById;
    Instant createdAt;
    long updatedById;
    Instant updatedAt;

}
