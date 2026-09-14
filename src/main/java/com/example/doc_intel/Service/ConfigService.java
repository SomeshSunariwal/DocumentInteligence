package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.ChatModel.AIConfigRequestDTO;
import com.example.doc_intel.DTO.ChatModel.AIConfigResponseDTO;
import com.example.doc_intel.Entity.AIConfig;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Exceptions.UserNotExistException;
import com.example.doc_intel.Repository.AIConfigRepository;
import com.example.doc_intel.Repository.UserRepository;
import com.example.doc_intel.Utils.Utils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final AIConfigRepository aiConfigRepository;

    private final UserRepository userRepository;

    public AIConfigResponseDTO addOrUpdateConfig(@NonNull AIConfigRequestDTO aiConfigRequestDTO) {
        String email = Utils.getUserEmail();
        // getConfig if present
        Optional<UserEntity> userEntity = userRepository.findByEmailAndIsActiveTrue(email);

        if (userEntity.isEmpty()) {
            throw new UserNotExistException("User Not Exist");
        }

        Optional<AIConfig> aiConfig = aiConfigRepository.findByUser_UserId(userEntity.get().getUserId());

        // if Config is already Present the update it.
        if (aiConfig.isPresent()) {
            aiConfig.get().setApiKey(aiConfigRequestDTO.getApiKey());
            aiConfig.get().setBaseURL(aiConfigRequestDTO.getBaseURL());
            aiConfig.get().setModelName(aiConfigRequestDTO.getModelName());
            aiConfig.get().setType(aiConfigRequestDTO.getType());
            return AIConfigResponseDTO.builder().message("Updated").build();
        }

        // if config is not present then create it.
        AIConfig aiConfigEntity = AIConfig.builder()
            .apiKey(aiConfigRequestDTO.getApiKey())
            .baseURL(aiConfigRequestDTO.getBaseURL())
            .modelName(aiConfigRequestDTO.getModelName())
            .type(aiConfigRequestDTO.getType())
            .user(userEntity.get())
            .build();

        aiConfigRepository.save(aiConfigEntity);
        return AIConfigResponseDTO.builder().message("Created").build();
    }
}
