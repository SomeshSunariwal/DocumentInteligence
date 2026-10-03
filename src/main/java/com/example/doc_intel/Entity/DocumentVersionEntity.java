package com.example.doc_intel.Entity;

import com.example.doc_intel.Enums.DocumentStatus;
import com.example.doc_intel.Enums.FileExtensions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "document_versions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_document_version_number",
        columnNames = {"document_id", "document_version"}
    )
)
public class DocumentVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Integer id;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", referencedColumnName = "document_id", nullable = false)
    private DocumentEntity document;

    @NonNull
    @Column(nullable = false)
    private String fileName;

    @NonNull
    @Column(nullable = false)
    private String objectKey;

    @NonNull
    @Column(nullable = false)
    private String bucketName;

    @NonNull
    @Column(nullable = false)
    private String contentType;

    @NonNull
    @Column(nullable = false)
    private Long fileSize;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileExtensions fileExtensions;

    @NonNull
    @Column(nullable = false)
    private Integer chunksCount;

    @NonNull
    @Column(nullable = false)
    private Integer documentVersion;

    @NonNull
    @Column(nullable = false)
    private String minIOVersionId;

    @NonNull
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @NonNull
    @Column(nullable = false)
    private String createdBy;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;
}
