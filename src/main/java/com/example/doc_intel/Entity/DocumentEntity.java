package com.example.doc_intel.Entity;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@Table(name = "documents")
public class DocumentEntity {

    @Id
    @GeneratedValue
    private UUID documentId;

    @NonNull
    private String fileName;

    @NonNull
    private LocalDateTime createdAt;

    @NonNull
    private String createdBy;

    @NonNull
    private LocalDateTime updateAt;

    @NonNull
    private String updatedBy;

    @NonNull
    private Boolean isActive;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
