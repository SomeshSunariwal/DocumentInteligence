package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.DocumentsDTO.DocumentResponseDTO;
import com.example.doc_intel.DTO.UserDTOs.UserDocumentsResponseDTO;
import com.example.doc_intel.Service.DocumentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


/**
 * Controller class for handling document-related API endpoints.
 */
@Tag(
        name = "Document Controller",
        description = "These APIs used to perform document related operations"
)
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/users/documents")
    public ResponseEntity<DocumentResponseDTO> uploadDocument(@RequestParam("file") MultipartFile file) {
        DocumentResponseDTO documentResponseDTO = documentService.uploadFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentResponseDTO);
    }

    @PutMapping("/users/documents/{documentId}")
    public ResponseEntity<DocumentResponseDTO> updateDocument(@PathVariable UUID documentId,
                                                              @RequestParam("file") MultipartFile file) {
        DocumentResponseDTO documentResponseDTO = documentService.updateDocument(documentId, file);
        return ResponseEntity.status(HttpStatus.OK).body(documentResponseDTO);
    }

    @DeleteMapping("/users/documents/{documentId}")
    public ResponseEntity<DocumentResponseDTO> deleteDocument(@PathVariable UUID documentId) {
        DocumentResponseDTO documentResponseDTO = documentService.deleteDocument(documentId);
        return ResponseEntity.status(HttpStatus.OK).body(documentResponseDTO);
    }

    @GetMapping("/users/documents")
    public ResponseEntity<UserDocumentsResponseDTO> getUsersAllDocuments() {
        UserDocumentsResponseDTO userAllDocuments = documentService.getUserAllDocuments();
        return ResponseEntity.status(HttpStatus.OK).body(userAllDocuments);
    }
}
