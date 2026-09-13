package com.example.doc_intel.LongChainChatModel;

import com.example.doc_intel.Entity.AIConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.localai.LocalAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LongChainLocalAIModel implements LongChainChatModel {

    @Override
    public ChatModel giveMeModel(AIConfig aiConfig) {

        log.info("Using Local AI Chat Model");

        return LocalAiChatModel.builder()
                .baseUrl(aiConfig.getBaseURL())
                .modelName(aiConfig.getModelName())
                .maxTokens(50)
//                .logRequests(true)
//                .logResponses(true)
                .build();
    }
}
