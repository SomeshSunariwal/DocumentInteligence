package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.UserDTOs.DeleteUserRequestDTO;
import com.example.doc_intel.DTO.UserDTOs.UserRequestDTO;
import com.example.doc_intel.DTO.UserDTOs.UserResponseDTO;
import com.example.doc_intel.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/user")
    public ResponseEntity<UserResponseDTO> addUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO userResponseDTO = userService.addUser(userRequestDTO);
        return ResponseEntity.ok().body(userResponseDTO);
    }

    @DeleteMapping("/user")
    public ResponseEntity<UserResponseDTO> deleteUser(@Valid @RequestBody DeleteUserRequestDTO deleteUserRequestDTO) {
        UserResponseDTO userResponseDTO = userService.deleteUser(deleteUserRequestDTO);
        return ResponseEntity.ok().body(userResponseDTO);
    }

}
