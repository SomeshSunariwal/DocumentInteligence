package com.example.doc_intel.Controller;

import com.example.doc_intel.DTO.DocumentsDTO.DocumentResponseDTO;
import com.example.doc_intel.DTO.DocumentsDTO.GetDocumentResponseDTO;
import com.example.doc_intel.DTO.UserDTOs.UserDocumentsResponseDTO;
import com.example.doc_intel.Service.DocumentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

/**
 * Controller class for handling document-related API endpoints.
 */
@Tag(
    name = "Document Controller",
    description = "These APIs used to perform document related operations"
)
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Endpoint to upload a document.
     * * @param files
     * @return DocumentResponseDTO
     */
    @PostMapping(value = "/users/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<DocumentResponseDTO>> uploadDocument(@RequestPart("files") List<MultipartFile> files) {
        List<DocumentResponseDTO> documentResponseDTOS = documentService.uploadFile(files);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentResponseDTOS);
    }

    /**
     * Endpoint to get all documents of a user.
     * @param documentId UUID documentId
     * @param file update document
     * @return DocumentResponseDTO
     */
    @PutMapping(value = "/users/documents/{documentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponseDTO> updateDocument(@PathVariable UUID documentId,
                                                              @RequestPart("file") MultipartFile file) {
        DocumentResponseDTO documentResponseDTO = documentService.updateDocument(documentId, file);
        return ResponseEntity.status(HttpStatus.OK).body(documentResponseDTO);
    }

    @DeleteMapping("/users/documents/{documentId}")
    public ResponseEntity<DocumentResponseDTO> deleteDocument(@NonNull @PathVariable UUID documentId) {
        DocumentResponseDTO documentResponseDTO = documentService.deleteDocument(documentId);
        return ResponseEntity.status(HttpStatus.OK).body(documentResponseDTO);
    }

    @GetMapping("/users/documents")
    public ResponseEntity<UserDocumentsResponseDTO> getUsersAllDocuments(
        @RequestParam(defaultValue = "0") @Min(0) int page) {
        UserDocumentsResponseDTO userAllDocuments = documentService.getUserAllDocuments(page);
        return ResponseEntity.status(HttpStatus.OK).body(userAllDocuments);
    }

    @GetMapping("/users/documents/{documentId}")
    public ResponseEntity<GetDocumentResponseDTO> getDocument(@NonNull @PathVariable UUID documentId) {
        GetDocumentResponseDTO getDocumentResponseDTO = documentService.getDocument(documentId);
        return ResponseEntity.status(HttpStatus.OK).body(getDocumentResponseDTO);
    }

    /**
     * This is used to summarize the document
     * @param documentId UUID
     * @return Stream of words
     */
    @GetMapping("/users/documents/{documentId}/summery")
    public ResponseEntity<ResponseBodyEmitter>  getDocumentSummery(@NonNull @PathVariable UUID documentId) {
        ResponseBodyEmitter sse = documentService.getDocumentSummery(documentId);
        return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(sse);
    }
}
