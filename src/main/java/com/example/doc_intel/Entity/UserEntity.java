package com.example.doc_intel.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@Table(name = "users",
        uniqueConstraints = {@UniqueConstraint(name = "uk_users_email", columnNames = "email")})
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @NonNull
    private String username;

    @NonNull
    private String firstName;

    @NonNull
    private String lastName;

    @NonNull
    @Column(unique = true, nullable = false)
    private String email;

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

}
