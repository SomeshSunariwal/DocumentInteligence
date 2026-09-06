package com.example.doc_intel.LongChainChatModel;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.localai.LocalAiChatModel;
import org.springframework.stereotype.Component;

@Component
public class LongChainLocalAIModel implements LongChainChatModel {
    @Override
    public ChatModel giveMeModel() {
        return LocalAiChatModel.builder()
                .baseUrl("http://127.0.0.1:1234/v1")
                .modelName("dolphin3.0-llama3.1-8b")
                .maxTokens(50)
//                .logRequests(true)
//                .logResponses(true)
                .build();
    }
}
