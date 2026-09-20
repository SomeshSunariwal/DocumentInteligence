package com.example.doc_intel.Controller;

import javax.annotation.Nullable;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import com.example.doc_intel.Service.ChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;

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

    /**
     * This method is used to get the stream response of chat messages
     *
     * @param documentId nullable
     * @param query Non-Nullable
     * @return @ResponseBodyEmitter
     */
    @Operation(
            summary = "Stream AI chat response",
            description = "Streams the AI-generated response for the provided question."
    )
    @GetMapping("/chat")
    public ResponseEntity<ResponseBodyEmitter> chat(@RequestParam(name = "documentId") @Nullable String documentId,
            @RequestParam(name = "query") @NotBlank String query) {
        ResponseBodyEmitter sse = chatService.chat(query, documentId);
        return ResponseEntity.ok().contentType(TEXT_EVENT_STREAM).body(sse);
    }
}
