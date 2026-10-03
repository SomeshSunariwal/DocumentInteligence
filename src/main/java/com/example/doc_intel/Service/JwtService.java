package com.example.doc_intel.Service;

import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Exceptions.UserNotExistException;
import com.example.doc_intel.Repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    private final SecretKey secretKey;

    private final UserRepository userRepository;

    public JwtService(@Value("${jwt.secret}") String secret, UserRepository userRepository) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.userRepository = userRepository;
    }

    public String generateToken(String username) {
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailAndIsActiveTrue(username);
        if (optionalUserEntity.isEmpty()) {
            throw new UserNotExistException("User Not Found", ErrorCode.LoginUserNotFound);
        }
        UserEntity userEntity = optionalUserEntity.get();
        return Jwts.builder()
            .subject(username)
            .issuer("DocumentIntelligence.Com")
            .claim("firstName", userEntity.getFirstName())
            .claim("lastName", userEntity.getLastName())
            .claim("username", userEntity.getUsername())
            .claim("email", userEntity.getEmail())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))
            .signWith(secretKey)
            .compact();
    }
}
