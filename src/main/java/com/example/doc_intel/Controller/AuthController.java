package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.UserDTOs.UserLoginRequestDTO;
import com.example.doc_intel.DTO.UserDTOs.UserLoginResponseDTO;
import com.example.doc_intel.Service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@RequestBody UserLoginRequestDTO userLoginRequestDTO) {
        UserLoginResponseDTO response = authService.login(userLoginRequestDTO);
        return ResponseEntity.ok(response);
    }
}
