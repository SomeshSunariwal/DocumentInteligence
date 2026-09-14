package com.example.doc_intel.LongChainChatModel;

import com.example.doc_intel.Entity.AIConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LongChainOpenAIChatModel implements LongChainChatModel {

    @Override
    public ChatModel giveMeModel(AIConfig aiConfig) {

        log.info("Using OpenAI Chat Model");
        return OpenAiChatModel.builder()
                .baseUrl(aiConfig.getBaseURL())
                .apiKey(aiConfig.getApiKey())
                .modelName(aiConfig.getModelName())
                .temperature(0.0)
                .build();
    }
}
