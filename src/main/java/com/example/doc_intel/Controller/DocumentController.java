package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.DocumentsDTO.DocumentResponseDTO;
import com.example.doc_intel.DTO.UserDTOs.UserDocumentsResponseDTO;
import com.example.doc_intel.Service.DocumentProcessService;
import com.example.doc_intel.Service.DocumentService;
import com.example.doc_intel.Service.PublisherService;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DocumentController {

    private final DocumentProcessService documentProcessService;

    private final PublisherService publisherService;

    private final DocumentService documentService;

//    @PostMapping("/document")
//    public ResponseEntity<DocumentUploadResponseDTO> postDocument(@Valid @RequestParam("file") MultipartFile file) {
//        DocumentUploadResponseDTO documentProcessResponseDTO = documentProcessService.processDocument(file);
//        return ResponseEntity.ok().body(documentProcessResponseDTO);
//    }

//    @PostMapping("/file")
//    public ResponseEntity<DocumentUploadResponseDTO> postFile(@Valid @RequestBody FileRequestDTO fileRequestDTO) {
//        DocumentUploadResponseDTO documentProcessResponseDTO = documentProcessService.processFile(fileRequestDTO);
//        return ResponseEntity.ok().body(documentProcessResponseDTO);
//    }

//    @PostMapping("/question")
//    public ResponseEntity<QuestionResponseDTO> postQuestion(@Valid @RequestBody QuestionRequestDTO questionDTO) {
//        QuestionResponseDTO responseDTO = documentProcessService.processQuestion(questionDTO);
//        return ResponseEntity.ok().body(responseDTO);
//    }

//    @PostMapping("/publish")
//    public ResponseEntity<DocumentUploadResponseDTO> publishDocument(@Valid @RequestBody FileRequestDTO fileRequestDTO) {
//        DocumentUploadResponseDTO documentProcessResponseDTO = publisherService.publishDocument(fileRequestDTO);
//        return ResponseEntity.ok().body(documentProcessResponseDTO);
//    }

    @PostMapping("/users/{email}/documents")
    public ResponseEntity<DocumentResponseDTO> uploadDocument(@PathVariable String email,
                                                              @RequestParam("file") MultipartFile file) {
        DocumentResponseDTO documentResponseDTO = documentService.uploadFile(email, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentResponseDTO);
    }

    @PutMapping("/users/{email}/documents/{documentId}")
    public ResponseEntity<DocumentResponseDTO> updateDocument(@PathVariable String email,
                                                              @PathVariable UUID documentId,
                                                              @RequestParam("file") MultipartFile file) {
        DocumentResponseDTO documentResponseDTO = documentService.updateDocument(email, documentId, file);
        return ResponseEntity.status(HttpStatus.OK).body(documentResponseDTO);
    }

    @DeleteMapping("/users/{email}/documents/{documentId}")
    public ResponseEntity<DocumentResponseDTO> deleteDocument(@PathVariable String email,
                                                              @PathVariable UUID documentId) {
        DocumentResponseDTO documentResponseDTO = documentService.deleteDocument(email, documentId);
        return ResponseEntity.status(HttpStatus.OK).body(documentResponseDTO);
    }

    @GetMapping("/users/{email}/documents")
    public ResponseEntity<UserDocumentsResponseDTO> getUsersAllDocuments(@PathVariable @Email String email) {
        UserDocumentsResponseDTO userAllDocuments = documentService.getUserAllDocuments(email);
        return ResponseEntity.status(HttpStatus.OK).body(userAllDocuments);
    }

}
