package com.example.doc_intel.Service;

import com.example.doc_intel.DTO.ChatModel.AIConfigRequestDTO;
import com.example.doc_intel.DTO.ChatModel.AIConfigResponseDTO;
import com.example.doc_intel.Entity.AIConfig;
import com.example.doc_intel.Entity.UserEntity;
import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.Exceptions.UserNotExistException;
import com.example.doc_intel.Repository.AIConfigRepository;
import com.example.doc_intel.Repository.UserRepository;
import com.example.doc_intel.Utils.Utils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final AIConfigRepository aiConfigRepository;

    private final UserRepository userRepository;

    @Transactional
    public AIConfigResponseDTO addOrUpdateConfig(@NonNull AIConfigRequestDTO aiConfigRequestDTO) {
        String email = Utils.getUserEmail();
        // getConfig if present
        Optional<UserEntity> userEntity = userRepository.findByEmailAndIsActiveTrue(email);

        if (userEntity.isEmpty()) {
            throw new UserNotExistException("User Not Exist", ErrorCode.ConfigUserNotFound);
        }
        Optional<AIConfig> aiConfig = aiConfigRepository.findByUser_UserId(userEntity.get().getUserId());

        // if Config is already Present the update it.
        if (aiConfig.isPresent()) {
            aiConfig.get().setApiKey(aiConfigRequestDTO.getApiKey());
            aiConfig.get().setBaseURL(aiConfigRequestDTO.getBaseURL());
            aiConfig.get().setModelName(aiConfigRequestDTO.getModelName());
            aiConfig.get().setType(aiConfigRequestDTO.getType());

            return AIConfigResponseDTO.builder()
                .type(aiConfig.get().getType())
                .modelName(aiConfig.get().getModelName())
                .baseURL(aiConfig.get().getBaseURL())
                .apiKey(aiConfig.get().getApiKey())
                .message("Config Updated Successfully")
                .build();
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
        return AIConfigResponseDTO.builder()
            .type(aiConfigEntity.getType())
            .modelName(aiConfigEntity.getModelName())
            .baseURL(aiConfigEntity.getBaseURL())
            .apiKey(aiConfigEntity.getApiKey())
            .message("Config Created Successfully")
            .build();
    }

    public AIConfigResponseDTO getConfig() {
        String email = Utils.getUserEmail();
        Optional<AIConfig> optionalAIConfig = aiConfigRepository.findByUser_Email(email);

        if (optionalAIConfig.isEmpty()) {
            throw new UserNotExistException("AI Config Not Found", ErrorCode.AIConfigNotFound);
        }
        AIConfig aiConfig = optionalAIConfig.get();

        return AIConfigResponseDTO.builder()
            .type(aiConfig.getType())
            .modelName(aiConfig.getModelName())
            .baseURL(aiConfig.getBaseURL())
            .apiKey(aiConfig.getApiKey())
            .message("Successfully Fetched Config")
            .build();
    }
}
