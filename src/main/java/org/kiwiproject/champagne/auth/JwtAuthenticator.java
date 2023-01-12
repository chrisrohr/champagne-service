package org.kiwiproject.champagne.auth;

import java.util.Optional;

import org.kiwiproject.champagne.jdbi.UserDao;

import io.dropwizard.auth.Authenticator;
import lombok.Builder;

@Builder
public class JwtAuthenticator implements Authenticator<JwtContext, UserPrincipal> {

    private UserDao userDao;

    @Override
    public Optional<UserPrincipal> authenticate(JwtContext context) {
        var userId = context.getClaims().getSignature();

        // TODO: We will probably want to add expiration dates to the validation to make
        //       sure that the token is still valid. Not sure yet where that goes.
        var optionalUser = userDao.findBySystemIdentifier(userId);
        return optionalUser.map(user -> UserPrincipal.builder().user(user).build());
    }
    
}
