package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.ChatModel.AIConfigRequestDTO;
import com.example.doc_intel.DTO.ChatModel.AIConfigResponseDTO;
import com.example.doc_intel.Service.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ConfigController {

    private final ConfigService configService;

    @PostMapping("/config/{email}")
    public ResponseEntity<AIConfigResponseDTO> addOrUpdateConfig(
        @PathVariable String email,
        @Valid @RequestBody AIConfigRequestDTO aiConfigRequestDTO) {

        AIConfigResponseDTO result = configService.addOrUpdateConfig(email, aiConfigRequestDTO);
        return ResponseEntity.ok().body(result);
    }
}
