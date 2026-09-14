package com.example.doc_intel.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {

        return Jwts.builder()
            .subject(username)
            .issuer("DocumentIntelligence.Com")
            .issuedAt(new Date())
            .content("User: " + username)
            .expiration(
                new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15))
            )
            .signWith(secretKey)
            .compact();
    }
}
