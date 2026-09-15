package com.example.doc_intel.Controller;


import com.example.doc_intel.Service.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/chat")
    public ResponseEntity<ResponseBodyEmitter> chat(@RequestParam String question) {
        ResponseBodyEmitter sse = chatService.chat(question);
        return ResponseEntity.ok().contentType(TEXT_EVENT_STREAM).body(sse);
    }
}
