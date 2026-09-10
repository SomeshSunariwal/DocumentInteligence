package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.UserDTOs.DeleteUserRequestDTO;
import com.example.doc_intel.DTO.UserDTOs.UserRequestDTO;
import com.example.doc_intel.DTO.UserDTOs.UserResponseDTO;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Exceptions.PSQLDBException;
import com.example.doc_intel.Repository.UserRepositoryImp;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepositoryImp userRepositoryImp;

    @Transactional
    public UserResponseDTO addUser(@NonNull UserRequestDTO userRequestDTO) {
        UUID uuid = UUID.nameUUIDFromBytes(userRequestDTO.getUsername().getBytes());

        // Convert Input Request Object to DataBase
        UserEntity userEntity = UserEntity.builder()
                .userId(uuid)
                .username(userRequestDTO.getUsername())
                .firstName(userRequestDTO.getFirstName())
                .lastName(userRequestDTO.getLastName())
                .email(userRequestDTO.getEmail())
                .createdAt(LocalDateTime.now())
                .createdBy(userRequestDTO.getEmail())
                .updateAt(LocalDateTime.now())
                .updatedBy(userRequestDTO.getEmail())
                .isActive(true)
                .build();

        try {
            UserEntity response = userRepositoryImp.add(userEntity);
            return UserResponseDTO.builder()
                    .uuid(response.getUserId())
                    .username(response.getUsername())
                    .firstName(response.getFirstName())
                    .lastName(response.getLastName())
                    .email(response.getEmail())
                    .build();
        } catch (DataIntegrityViolationException e) {
            throw new PSQLDBException(e.getMostSpecificCause().getMessage());
        }
    }

    @Transactional
    public UserResponseDTO deleteUser(@NonNull DeleteUserRequestDTO deleteUserRequestDTO) {

        // Convert Input Request Object to DataBase
        UserEntity userEntity = UserEntity.builder()
                .email(deleteUserRequestDTO.getEmail())
                .build();

        UserEntity response = userRepositoryImp.deleteByEmailId(userEntity);

        return UserResponseDTO.builder()
                .username(response.getUsername())
                .email(response.getEmail())
                .build();
    }


}
