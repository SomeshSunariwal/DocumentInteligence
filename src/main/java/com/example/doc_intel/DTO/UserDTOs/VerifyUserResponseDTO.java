package com.example.doc_intel.DTO.UserDTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class VerifyUserResponseDTO {

    @NotBlank
    private Boolean validate;
}
