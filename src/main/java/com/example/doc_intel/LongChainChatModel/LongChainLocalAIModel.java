package com.example.doc_intel.LongChainChatModel;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.localai.LocalAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LongChainLocalAIModel implements LongChainChatModel {
    private static final Logger log = LoggerFactory.getLogger(LongChainLocalAIModel.class);

    @Override
    public ChatModel giveMeModel() {
        log.info("Using Local AI Chat Model");
        return LocalAiChatModel.builder()
                .baseUrl("http://127.0.0.1:1234/v1")
                .modelName("dolphin3.0-llama3.1-8b")
                .maxTokens(50)
//                .logRequests(true)
//                .logResponses(true)
                .build();
    }
}
