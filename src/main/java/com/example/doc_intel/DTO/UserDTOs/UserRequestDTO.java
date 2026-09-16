package com.example.doc_intel.DTO.UserDTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {

    @NotBlank
    @Schema(defaultValue = "username")
    private String username;

    @NotBlank
    @Schema(defaultValue = "firstName")
    private String firstName;

    @NotBlank
    @Schema(defaultValue = "lastName")
    private String lastName;

    @NotBlank
    @Email
    @Schema(defaultValue = "user@example.com")
    private String email;

    @NotBlank
    @Schema(defaultValue = "password")
    private String password;

}
