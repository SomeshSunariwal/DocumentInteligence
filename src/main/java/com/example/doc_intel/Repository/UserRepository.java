package com.example.doc_intel.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.example.doc_intel.Entity.UserEntity;

import lombok.NonNull;

@Component
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    UserEntity deleteByEmail(@NonNull String email);

    UserEntity findByEmailAndIsActiveTrue(@NonNull String email);
}
