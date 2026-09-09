package com.example.doc_intel.Repository;

import com.example.doc_intel.Entity.DocumentEntity;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryImp {

    private UserRepository UserRepository;

    public UserRepositoryImp(UserRepository UserRepository) {
        this.UserRepository = UserRepository;
    }

    public void add(DocumentEntity documentEntity) {
        UserRepository.save(documentEntity);
    }
}
