package com.example.doc_intel.DocumentProcesser;

import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.DTO.KafkaEventDTO;
import com.example.doc_intel.Entity.DocumentEntity;
import com.example.doc_intel.Entity.DocumentVersionEntity;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Exceptions.DBExceptions.DocumentNotExistException;
import com.example.doc_intel.MinIOProcesser.MinIOProcessor;
import com.example.doc_intel.Repository.DocumentsRepository;
import com.example.doc_intel.Repository.DocumentVersionsRepository;
import com.example.doc_intel.Repository.UserRepository;
import io.minio.ObjectWriteResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DocumentProcessorTest {

    @Test
    void derivedLockMethodResolvesDocumentOwnerAndActiveFlag() throws Exception {
        var method = DocumentsRepository.class.getMethod(
            "findForUpdateByDocumentIdAndUser_EmailAndIsActiveTrue", UUID.class, String.class);
        var query = new org.springframework.data.repository.query.parser.PartTree(method.getName(), DocumentEntity.class);
        assertEquals(java.util.List.of("documentId", "user.email", "isActive"), query.getParts().stream()
            .map(part -> part.getProperty().toDotPath()).toList());
        assertEquals(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE,
            method.getAnnotation(org.springframework.data.jpa.repository.Lock.class).value());
    }

    private final MinIOProcessor minio = mock(MinIOProcessor.class);

    private final UserRepository users = mock(UserRepository.class);

    private final DocumentsRepository documents = mock(DocumentsRepository.class);

    private final DocumentVersionsRepository versions = mock(DocumentVersionsRepository.class);

    private final DocumentProcessor processor = new DocumentProcessor(minio, users, documents, versions);

    @Test
    void locksDocumentBeforeReadingLatestVersionAndCreatesNextVersion() {
        UUID id = UUID.randomUUID();
        String email = "user@example.com";
        var user = mock(UserEntity.class);
        var document = mock(DocumentEntity.class);
        var previous = mock(DocumentVersionEntity.class);
        var upload = mock(ObjectWriteResponse.class);
        var file = new MockMultipartFile("file", "update.txt", "text/plain", new byte[] {1});
        when(users.findByEmailAndIsActiveTrue(email)).thenReturn(Optional.of(user));
        when(documents.findForUpdateByDocumentIdAndUser_EmailAndIsActiveTrue(id, email)).thenReturn(Optional.of(document));
        when(versions.findFirstByDocument_DocumentIdAndDocument_IsActiveTrueOrderByDocumentVersionDesc(id))
            .thenReturn(Optional.of(previous));
        when(previous.getDocumentVersion()).thenReturn(5);
        when(previous.getObjectKey()).thenReturn("object-key");
        when(previous.getBucketName()).thenReturn("bucket");
        when(minio.putObject(file, "object-key")).thenReturn(upload);
        when(upload.versionId()).thenReturn("minio-version");
        when(user.getUserId()).thenReturn(UUID.randomUUID());
        when(document.getDocumentId()).thenReturn(id);
        when(document.getUpdateAt()).thenReturn(java.time.LocalDateTime.now());
        when(versions.save(any(DocumentVersionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var events = new ArrayList<KafkaEventDTO>();

        var response = processor.processDocumentUpdate(email, id, file, events);

        var order = inOrder(documents, versions, minio);
        order.verify(documents).findForUpdateByDocumentIdAndUser_EmailAndIsActiveTrue(id, email);
        order.verify(versions).findFirstByDocument_DocumentIdAndDocument_IsActiveTrueOrderByDocumentVersionDesc(id);
        order.verify(minio).putObject(file, "object-key");
        order.verify(versions).save(any(DocumentVersionEntity.class));
        assertEquals(6, response.getVersion());
        assertEquals(6, events.getFirst().getDocumentVersion());
    }

    @Test
    void missingOrUnauthorizedLockedDocumentStopsBeforeVersionLookupOrUpload() {
        UUID id = UUID.randomUUID();
        String email = "user@example.com";
        when(users.findByEmailAndIsActiveTrue(email)).thenReturn(Optional.of(mock(UserEntity.class)));
        when(documents.findForUpdateByDocumentIdAndUser_EmailAndIsActiveTrue(id, email)).thenReturn(Optional.empty());
        var file = new MockMultipartFile("file", "update.txt", "text/plain", new byte[] {1});
        var error = assertThrows(DocumentNotExistException.class,
            () -> processor.processDocumentUpdate(email, id, file, new ArrayList<>()));
        assertEquals(ErrorCode.DocumentUpdateTargetNotFound, error.getErrorCode());
        verifyNoInteractions(versions, minio);
    }
}
