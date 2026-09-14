package com.example.doc_intel.DTO.ChatModel;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchRequestDTO {

    @NotBlank
    String searchTerm;

}
