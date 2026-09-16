package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.UserDTOs.UserLoginRequestDTO;
import com.example.doc_intel.DTO.UserDTOs.UserLoginResponseDTO;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    public UserLoginResponseDTO login(@NonNull UserLoginRequestDTO request) {

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassphrase()
            )
        );

        String token = jwtService.generateToken(request.getUsername());
        return UserLoginResponseDTO.builder()
            .email(request.getUsername())
            .token(token).build();
    }
}
