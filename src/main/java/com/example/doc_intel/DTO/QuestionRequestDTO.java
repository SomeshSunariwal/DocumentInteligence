package com.example.doc_intel.DTO;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionRequestDTO {
    @NotBlank
    String question;
}
