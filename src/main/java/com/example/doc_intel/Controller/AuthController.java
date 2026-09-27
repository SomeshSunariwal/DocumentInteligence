package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.UserDTOs.UserLoginRequestDTO;
import com.example.doc_intel.DTO.UserDTOs.UserLoginResponseDTO;
import com.example.doc_intel.DTO.UserDTOs.VerifyUserResponseDTO;
import com.example.doc_intel.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Login Controller",
        description = "Handles user login operations"
)
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Handles user authentication",
        description = "Provides endpoints for user login"
    )
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@RequestBody UserLoginRequestDTO userLoginRequestDTO) {
        UserLoginResponseDTO response = authService.login(userLoginRequestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<VerifyUserResponseDTO> verify() {
        VerifyUserResponseDTO  response = authService.verify();
        return ResponseEntity.ok(response);
    }
}
