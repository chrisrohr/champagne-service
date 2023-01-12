package org.kiwiproject.champagne.auth;

import java.security.Principal;

import org.kiwiproject.champagne.core.User;

import lombok.Builder;
import lombok.experimental.Delegate;

@Builder
public class UserPrincipal implements Principal {

    @Delegate
    private final User user;

    @Override
    public String getName() {
        return user.getSystemIdentifier();
    }
    
}
