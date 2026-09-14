package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.ChatModel.AIConfigRequestDTO;
import com.example.doc_intel.DTO.ChatModel.AIConfigResponseDTO;
import com.example.doc_intel.Service.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * Controller class for handling configuration-related API endpoints.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class ConfigController {

    private final ConfigService configService;

    @PostMapping("/config")
    public ResponseEntity<AIConfigResponseDTO> addOrUpdateConfig(
        @Valid @RequestBody AIConfigRequestDTO aiConfigRequestDTO) {

        AIConfigResponseDTO result = configService.addOrUpdateConfig(aiConfigRequestDTO);
        return ResponseEntity.ok().body(result);
    }
}
