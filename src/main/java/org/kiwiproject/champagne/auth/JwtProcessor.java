package org.kiwiproject.champagne.auth;

import java.security.Key;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

public class JwtProcessor {
    
    private final JwtParser parser;
    private final Key key;

    public JwtProcessor(Key key) {
        this.parser = Jwts.parserBuilder().setSigningKey(key).build();
        this.key = key;
    }

    public JwtContext processToken(String token) {
        var claims = parser.parseClaimsJws(token);

        return JwtContext.builder()
            .token(token)
            .claims(claims)
            .build();
    }

    public String createToken(String userId) {
        return Jwts.builder()
            .setSubject(userId)
            .signWith(key)
            .compact();
    }
}
