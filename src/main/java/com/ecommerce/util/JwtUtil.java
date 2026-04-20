package com.ecommerce.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    public String generateToken(String email,String role){
            return Jwts.builder().
                    setSubject(email).
                    claim("role",role).
                    setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis()+1000*60*60))
                    .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .compact();
    }

    public String extractEmail(String token){
        return Jwts.parserBuilder().setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(secret.getBytes(StandardCharsets.UTF_8)).build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {

            return false;
        }
    }
}
