package com.example.doc_intel.Service;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String generateToken(String username) {

        return Jwts.builder()
            .subject(username)
            .issuer("DocumentIntelligence.Com")
            .issuedAt(new Date())
            .expiration(
                new Date(System.currentTimeMillis() + 1000 * 60 * 60)
            )
            .signWith(secretKey)
            .compact();
    }
}
