package org.kiwiproject.champagne.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Delegate;

/**
 * Core model for user information in relation to a specific deployable system. This model delegates a true User object.
 */
@Value
@Builder
public class UserInSystem {

    @Delegate
    User user;

    boolean systemAdmin;
}
