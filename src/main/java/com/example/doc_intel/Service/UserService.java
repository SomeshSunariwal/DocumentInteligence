package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.UserDTOs.UserRequestDTO;
import com.example.doc_intel.DTO.UserDTOs.UserResponseDTO;
import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Exceptions.PSQLDBException;
import com.example.doc_intel.Exceptions.UnAuthenticatedUser;
import com.example.doc_intel.Exceptions.UserNotExistException;
import com.example.doc_intel.Repository.UserRepository;
import com.example.doc_intel.Utils.Utils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * This method use to add the user in the database
     *
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
            throw new PSQLDBException(e.getMostSpecificCause().getMessage(), ErrorCode.UserDatabaseOperationFailed);
        }
    }

    /**
     * This method is used to delete the user using EmailId
     *
     * @return UserName and Email in response
     */
    @Transactional
    public UserResponseDTO deleteUser() {
        String email = Utils.getUserEmail();

        Optional<UserEntity> optionalResponse = userRepository.findByEmailAndIsActiveTrue(email);
        if (optionalResponse.isEmpty()) {
            throw new UserNotExistException("User Not Found", ErrorCode.UserDeleteTargetNotFound);
        }
        UserEntity userEntity = optionalResponse.get();
        UserEntity deletedUser = userRepository.deleteByEmail(userEntity.getEmail());

        return UserResponseDTO.builder()
            .userId(deletedUser.getUserId())
            .firstName(deletedUser.getFirstName())
            .lastName(deletedUser.getLastName())
            .username(deletedUser.getUsername())
            .email(deletedUser.getEmail())
            .build();
    }

    /**
     * This method is used to softly delete the user
     *
     * @param email input parameter
     * @return user object
     */
    @Transactional
    public UserResponseDTO softDeleteUser(@NonNull String email) {
        String authUserEmail = Utils.getUserEmail();
        if (!authUserEmail.equals(email)) {
            throw new UnAuthenticatedUser("You are not the owner of Email", ErrorCode.UserEmailOwnershipMismatch);
        }
        // Convert Input Request Object to DataBase
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (optionalUserEntity.isEmpty()) {
            throw new UserNotExistException("User Not Found", ErrorCode.UserSoftDeleteTargetNotFound);
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

    public UserResponseDTO getUser() {
        String email = Utils.getUserEmail();

        Optional<UserEntity> optionalUserEntity = userRepository.findByEmailAndIsActiveTrue(email);
        if (optionalUserEntity.isEmpty()) {
            throw new UserNotExistException("User Not Found", ErrorCode.UserGetUserNotFound);
        }
        UserEntity userEntity = optionalUserEntity.get();

        return UserResponseDTO.builder()
            .userId(userEntity.getUserId())
            .username(userEntity.getUsername())
            .firstName(userEntity.getFirstName())
            .lastName(userEntity.getLastName())
            .email(userEntity.getEmail())
            .build();
    }
}
