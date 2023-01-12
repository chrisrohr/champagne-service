package org.kiwiproject.champagne.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
class JwtContext {
    
    private final String token;
    private final Jws<Claims> claims;
}
