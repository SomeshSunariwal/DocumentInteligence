package com.example.doc_intel.DTO.ChatModel;

import com.example.doc_intel.Enums.ChatModelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AIConfigRequestDTO {

    @NotNull
    private ChatModelType type;

    @NotBlank
    private String modelName;

    @NotBlank
    private String baseURL;

    @NotNull
    private String apiKey;
}
