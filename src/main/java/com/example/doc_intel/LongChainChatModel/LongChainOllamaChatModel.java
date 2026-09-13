package com.example.doc_intel.LongChainChatModel;

import com.example.doc_intel.Entity.AIConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LongChainOllamaChatModel implements LongChainChatModel {

    @Override
    public ChatModel giveMeModel(AIConfig aiConfig) {

        log.info("Using Ollama Chat Model");
        return OllamaChatModel
                .builder()
                .baseUrl(aiConfig.getBaseURL())
                .modelName(aiConfig.getModelName())
                .temperature(0.0)
                .build();
    }
}
