package com.example.doc_intel.DTO.UserDTOs;

import com.example.doc_intel.DTO.DocumentsDTO.DocumentResponseDTO;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDocumentsResponseDTO {

    @NotNull
    private UUID userId;

    @NotBlank
    private String username;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @Nullable
    private List<DocumentResponseDTO> documents = new ArrayList<>();

}
