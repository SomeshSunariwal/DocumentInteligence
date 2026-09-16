package com.example.doc_intel.DTO.ChatModel;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchRequestDTO {

    @NotBlank
    @Schema(defaultValue = "What is java?")
    String searchTerm;

}
