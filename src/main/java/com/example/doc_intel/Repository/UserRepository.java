package com.example.doc_intel.Repository;

import com.example.doc_intel.Entity.UserEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    UserEntity deleteByEmail(@NonNull String email);

    UserEntity findByUsername(@NonNull String userName);
}
