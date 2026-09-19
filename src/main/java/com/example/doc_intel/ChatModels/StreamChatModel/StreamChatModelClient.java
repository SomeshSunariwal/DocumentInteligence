package com.example.doc_intel.ChatModels.StreamChatModel;

import com.example.doc_intel.Entity.AIConfig;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.stereotype.Component;

@Component
public class StreamChatModelClient implements StreamChatModel {

    @Override
    public StreamingChatModel giveMeModel(AIConfig aiConfig) {
        return OpenAiStreamingChatModel.builder()
            .baseUrl(aiConfig.getBaseURL())
            .apiKey(aiConfig.getApiKey())
            .modelName(aiConfig.getModelName())
            .maxTokens(1000)
            .build();
    }
}
