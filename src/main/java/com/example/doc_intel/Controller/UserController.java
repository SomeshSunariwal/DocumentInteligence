package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.UserDTOs.DeleteUserRequestDTO;
import com.example.doc_intel.DTO.UserDTOs.UserRequestDTO;
import com.example.doc_intel.DTO.UserDTOs.UserResponseDTO;
import com.example.doc_intel.Service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * This mapping will create a new user in the DB
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> addUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO userResponseDTO = userService.addUser(userRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }

    /**
     * This function delete the user from the table.
     */
    @DeleteMapping
    public ResponseEntity<UserResponseDTO> deleteUser(@Valid @RequestBody DeleteUserRequestDTO deleteUserRequestDTO) {
        UserResponseDTO userResponseDTO = userService.deleteUser(deleteUserRequestDTO);
        return ResponseEntity.ok().body(userResponseDTO);
    }

    /**
     * This function soft delete the user from the table.
     * it will make the user inactive.
     */
    @DeleteMapping("/{email}")
    public ResponseEntity<UserResponseDTO> deleteUser(@Email @PathVariable String email) {
        UserResponseDTO userResponseDTO = userService.softDeleteUser(email);
        return ResponseEntity.ok().body(userResponseDTO);
    }

}
