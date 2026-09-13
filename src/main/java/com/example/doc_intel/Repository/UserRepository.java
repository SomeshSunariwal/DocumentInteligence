package com.example.doc_intel.Repository;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.example.doc_intel.Entity.UserEntity;

import lombok.NonNull;

import java.util.Optional;

@Component
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByEmailAndIsActiveTrue(@NonNull String email);

    Optional<UserEntity> deleteByEmail(@NotBlank String email);

    Optional<UserEntity> findByEmail(@NotBlank String email);
}
