package com.example.doc_intel.Repository;

import com.example.doc_intel.Entity.UserEntity;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImp {

    private final UserRepository UserRepository;

    public UserEntity add(@NonNull UserEntity userEntity) {
        return UserRepository.save(userEntity);
    }

    public UserEntity deleteByEmailId(@NonNull UserEntity userEntity) {
        return UserRepository.deleteByEmail(userEntity.getEmail());
    }

    public UserEntity findByEmailAndIsActiveTrue(@NonNull String email) {
        return UserRepository.findByEmailAndIsActiveTrue(email);
    }
}
