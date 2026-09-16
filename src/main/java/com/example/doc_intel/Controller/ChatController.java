package com.example.doc_intel.Controller;


import com.example.doc_intel.Service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

@Tag(
        name = "Chat Operation",
        description = "This API is used to get the stream response"
)
@RestController
@RequestMapping("/api")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;

    @Operation(
            summary = "Stream AI chat response",
            description = "Streams the AI-generated response for the provided question."
    )
    @GetMapping("/chat")
    public ResponseEntity<ResponseBodyEmitter> chat(@RequestParam String question) {
        ResponseBodyEmitter sse = chatService.chat(question);
        return ResponseEntity.ok().contentType(TEXT_EVENT_STREAM).body(sse);
    }
}
