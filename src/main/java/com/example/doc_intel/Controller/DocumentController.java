package com.example.doc_intel.Controller;

import com.example.doc_intel.Service.DocumentProcessService;
import com.example.doc_intel.Service.PublisherService;
import com.example.doc_intel.DTO.DocumentProcessResponseDTO;
import com.example.doc_intel.DTO.FileRequestDTO;
import com.example.doc_intel.DTO.QuestionRequestDTO;
import com.example.doc_intel.DTO.QuestionResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class DocumentController {

    @Autowired
    private DocumentProcessService documentProcessService;

    @Autowired
    private PublisherService publisherService;

    @PostMapping("/document")
    public ResponseEntity<DocumentProcessResponseDTO> postDocument(@RequestParam("file") MultipartFile file) {
        DocumentProcessResponseDTO documentProcessResponseDTO = documentProcessService.processDocument(file);
        return ResponseEntity.ok().body(documentProcessResponseDTO);
    }

    @PostMapping("/file")
    public ResponseEntity<DocumentProcessResponseDTO> postFile(@RequestBody FileRequestDTO fileRequestDTO) {
        DocumentProcessResponseDTO documentProcessResponseDTO = documentProcessService.processFile(fileRequestDTO);
        return ResponseEntity.ok().body(documentProcessResponseDTO);
    }

    @PostMapping("/question")
    public ResponseEntity<QuestionResponseDTO> postQuestion(@RequestBody QuestionRequestDTO questionDTO) {
        QuestionResponseDTO responseDTO = documentProcessService.processQuestion(questionDTO);
        return ResponseEntity.ok().body(responseDTO);
    }

    @PostMapping("/publish")
    public ResponseEntity<DocumentProcessResponseDTO> publishDocument(@RequestBody FileRequestDTO fileRequestDTO)  {
        DocumentProcessResponseDTO documentProcessResponseDTO = publisherService.publishDocument(fileRequestDTO);
        return ResponseEntity.ok().body(documentProcessResponseDTO);
    }
}
