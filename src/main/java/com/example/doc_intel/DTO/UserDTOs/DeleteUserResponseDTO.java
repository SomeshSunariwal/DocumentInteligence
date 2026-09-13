package com.example.doc_intel.DTO.UserDTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteUserResponseDTO {

    @NotBlank
    private String username;

    @NonNull
    private String version;

}
