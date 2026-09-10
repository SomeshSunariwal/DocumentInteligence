package com.example.doc_intel.Entity;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "documents")
public class DocumentEntity {

    @Id
    @Column(nullable = false)
    private UUID documentId;

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

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String createdBy;

    @NonNull
    @Column(nullable = false)
    private LocalDateTime updateAt;

    @NonNull
    @Column(nullable = false)
    private String updatedBy;

    @NonNull
    @Column(nullable = false)
    private Boolean isActive;

    @NonNull
    @Column(nullable = false)
    private Integer version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private UserEntity user;
}
