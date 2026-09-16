package com.example.doc_intel.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.doc_intel.DTO.UserDTOs.UserLoginRequestDTO;
import com.example.doc_intel.DTO.UserDTOs.UserLoginResponseDTO;

public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_shouldReturnUserLoginResponseWhenAuthenticationSucceeds() {
        // Arrange
        String username = "testuser";
        String passphrase = "password";
        String token = "mock-jwt-token";

        UserLoginRequestDTO request = new UserLoginRequestDTO(username, passphrase);

        // Mock the authentication process to return a successful Authentication object
        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(mockAuthentication);

        // Mock the JWT token generation
        when(jwtService.generateToken(username)).thenReturn(token);

        // Act
        UserLoginResponseDTO response = authService.login(request);

        // Assert
        assertEquals(username, response.getEmail());
        assertEquals(token, response.getToken());

        // Verify interactions
        verify(authenticationManager).authenticate(
            argThat(tk -> tk.getName().equals(username) && tk.getCredentials().equals(passphrase))
        );
        verify(jwtService).generateToken(username);
    }

    @Test
    void login_shouldThrowExceptionWhenAuthenticationFails() {
        // Arrange
        String username = "invaliduser";
        String passphrase = "wrongpassword";
        UserLoginRequestDTO request = new UserLoginRequestDTO(username, passphrase);

        // Mock the authentication process to throw an exception (simulating failed login)
        RuntimeException authException = new RuntimeException("Authentication failed");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(authException);

        // Act & Assert
        // We expect the method to throw an exception if authentication fails, 
        // as per Spring Security's behavior when authenticate() throws an exception.
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        }, "Should throw RuntimeException when authentication fails");

        // Verify JWT token generation was NOT called
        verify(jwtService, never()).generateToken(anyString());
    }
}