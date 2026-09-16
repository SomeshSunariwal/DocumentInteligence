package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.ChatModel.AIConfigRequestDTO;
import com.example.doc_intel.DTO.ChatModel.AIConfigResponseDTO;
import com.example.doc_intel.Service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(
        name = "Config Controller",
        description = "API for managing AI configuration"
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
public class ConfigController {

    private final ConfigService configService;

    @Operation(
            summary = "Add or Update AI Configuration",
            description = "Allows users to configure AI settings"
    )
    @PostMapping("/config")
    public ResponseEntity<AIConfigResponseDTO> addOrUpdateConfig(
            @Valid @RequestBody AIConfigRequestDTO aiConfigRequestDTO) {

        AIConfigResponseDTO result = configService.addOrUpdateConfig(aiConfigRequestDTO);
        return ResponseEntity.ok().body(result);
    }
}
