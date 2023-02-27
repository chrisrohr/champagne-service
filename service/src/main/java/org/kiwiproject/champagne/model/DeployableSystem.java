package org.kiwiproject.champagne.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import javax.validation.constraints.NotBlank;

@Builder
@Value
public class DeployableSystem {

    Long id;
    Instant createdAt;
    Instant updatedAt;

    @NotBlank
    String name;

    Long devEnvironmentId;
    String environmentPromotionOrder;

    /**
     * This is a transitive property populated through the users_system table and indicates if a specific user is
     * an admin in this system.  Listing all systems will NOT populate this field.
     */
    @With
    boolean admin;
}
