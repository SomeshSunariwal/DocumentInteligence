package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.DocumentUploadResponseDTO;
import com.example.doc_intel.DTO.QuestionResponseDTO;
import com.example.doc_intel.DTO.FileRequestDTO;
import com.example.doc_intel.DTO.QuestionRequestDTO;
import com.example.doc_intel.DTO.DocumentResponseDTO;
import com.example.doc_intel.Service.DocumentProcessService;
import com.example.doc_intel.Service.FileHandlingService;
import com.example.doc_intel.Service.PublisherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DocumentController {

    private final DocumentProcessService documentProcessService;

    private final PublisherService publisherService;

    private final FileHandlingService fileHandlingService;

    @PostMapping("/document")
    public ResponseEntity<DocumentUploadResponseDTO> postDocument(@Valid @RequestParam("file") MultipartFile file) {
        DocumentUploadResponseDTO documentProcessResponseDTO = documentProcessService.processDocument(file);
        return ResponseEntity.ok().body(documentProcessResponseDTO);
    }

    @PostMapping("/file")
    public ResponseEntity<DocumentUploadResponseDTO> postFile(@Valid @RequestBody FileRequestDTO fileRequestDTO) {
        DocumentUploadResponseDTO documentProcessResponseDTO = documentProcessService.processFile(fileRequestDTO);
        return ResponseEntity.ok().body(documentProcessResponseDTO);
    }

    @PostMapping("/question")
    public ResponseEntity<QuestionResponseDTO> postQuestion(@Valid @RequestBody QuestionRequestDTO questionDTO) {
        QuestionResponseDTO responseDTO = documentProcessService.processQuestion(questionDTO);
        return ResponseEntity.ok().body(responseDTO);
    }

    @PostMapping("/publish")
    public ResponseEntity<DocumentUploadResponseDTO> publishDocument(@Valid @RequestBody FileRequestDTO fileRequestDTO)  {
        DocumentUploadResponseDTO documentProcessResponseDTO = publisherService.publishDocument(fileRequestDTO);
        return ResponseEntity.ok().body(documentProcessResponseDTO);
    }

    @PostMapping("/user/{username}/document")
    public ResponseEntity<DocumentResponseDTO> testFileUpload(@RequestParam String userName,
                                                                @RequestParam("file") MultipartFile file)  {
        DocumentResponseDTO documentResponseDTO = fileHandlingService.uploadFile(userName, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentResponseDTO);
    }

}
