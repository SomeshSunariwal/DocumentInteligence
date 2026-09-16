package com.example.doc_intel.DTO.UserDTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginRequestDTO {

    @NotNull
    @Schema(defaultValue = "user@example.com")
    private String username;

    @NotNull
    @Schema(defaultValue = "password")
    private String passphrase;
}
