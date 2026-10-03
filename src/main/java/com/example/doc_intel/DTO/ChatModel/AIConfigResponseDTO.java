package com.example.doc_intel.DTO.ChatModel;

import com.example.doc_intel.Enums.ChatModelType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AIConfigResponseDTO {

    @NotBlank
    private ChatModelType type;

    @NotBlank
    private String modelName;

    @NotBlank
    private String baseURL;

    @NotBlank
    private String apiKey;
}
