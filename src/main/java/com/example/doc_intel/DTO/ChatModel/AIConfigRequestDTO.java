package com.example.doc_intel.DTO.ChatModel;

import com.example.doc_intel.Enums.ChatModelType;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(defaultValue = "LOCAL")
    private ChatModelType type;

    @NotBlank
    @Schema(defaultValue = "google/gemma-4-e2b")
    private String modelName;

    @NotBlank
    @Schema(defaultValue = "http://127.0.0.1:1234/v1")
    private String baseURL;

    @NotNull
    @Schema(defaultValue = "apiKey")
    private String apiKey;
}
