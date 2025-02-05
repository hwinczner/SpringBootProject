package com.SpringBoot.Project.Security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;


@Component
public class JwtGenerator {


    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public String generateToken(Authentication authentication){
        String user = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + SecurityConstants.JWT_EXPiRATION);

        String token = Jwts.builder()
                .setSubject(user)
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .signWith(key)
                .compact();

        return token;

    }

    public String getUserName(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Log the actual exception message for debugging purposes
            System.err.println("JWT validation failed: " + e.getMessage());
            e.printStackTrace();
            throw new AuthenticationCredentialsNotFoundException("JWT incorrect or expired", e);
        }
    }
}
