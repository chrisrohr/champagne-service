package org.kiwiproject.champagne.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.List;
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

    /**
     * Transitive property that is the list of users assigned to this system and if they are an admin
     */
    @With
    List<SystemUser> users;

    @Builder
    @Value
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ToString
    public static class SystemUser {
        Long userId;
        boolean admin;
    }
}
