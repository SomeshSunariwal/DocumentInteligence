package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.UserDTOs.DeleteUserRequestDTO;
import com.example.doc_intel.DTO.UserDTOs.UserRequestDTO;
import com.example.doc_intel.DTO.UserDTOs.UserResponseDTO;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Exceptions.PSQLDBException;
import com.example.doc_intel.Exceptions.UserNotExistException;
import com.example.doc_intel.Repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * This method use to add the user in the database
     * @param userRequestDTO Input Request parameter
     * @return UserResponseDTO
     */
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
                // Password should be encrypted.
                .passphrase(passwordEncoder.encode(userRequestDTO.getPassword()))
                .build();

        try {
            UserEntity response = userRepository.save(userEntity);
            return UserResponseDTO.builder()
                    .userId(response.getUserId())
                    .username(response.getUsername())
                    .firstName(response.getFirstName())
                    .lastName(response.getLastName())
                    .email(response.getEmail())
                    .build();
        } catch (DataIntegrityViolationException e) {
            throw new PSQLDBException(e.getMostSpecificCause().getMessage());
        }
    }

    /**
     * This method is used to delete the user using EmailId
     * @param deleteUserRequestDTO input to delete the User By Email Id
     * @return UserName and Email in response
     */
    @Transactional
    public UserResponseDTO deleteUser(@NonNull DeleteUserRequestDTO deleteUserRequestDTO) {

        // Convert Input Request Object to DataBase
        UserEntity userEntity = UserEntity.builder()
                .email(deleteUserRequestDTO.getEmail())
                .build();

        Optional<UserEntity> optionalResponse = userRepository.deleteByEmail(userEntity.getEmail());
        if(optionalResponse.isEmpty()) {
            throw new UserNotExistException("User Not Found");
        }
        UserEntity response = optionalResponse.get();

        return UserResponseDTO.builder()
                .username(response.getUsername())
                .email(response.getEmail())
                .build();
    }

    /**
     * This method is used to softly delete the user
     * @param email input parameter
     * @return user object
     */
    @Transactional
    public UserResponseDTO softDeleteUser(@NonNull String email) {

        // Convert Input Request Object to DataBase
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if(optionalUserEntity.isEmpty()) {
            throw new UserNotExistException("User Not Found");
        }

        UserEntity userEntity = optionalUserEntity.get();
        // Soft deleting the user
        userEntity.setIsActive(false);

        return UserResponseDTO.builder()
                .userId(userEntity.getUserId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .build();
    }

}
