package com.example.doc_intel.LongChainChatModel;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
public class LongChainOllamaChatModel implements LongChainChatModel {

    private static final Logger log = LoggerFactory.getLogger(LongChainOllamaChatModel.class);
    private final String BASE_URL = "http://127.0.0.1:1234/v1";
    private final String MODEL_NAME = "dolphin3.0-llama3.1-8b";

    @Override
    public ChatModel giveMeModel() {
        log.info("Using Ollama Chat Model");
        return OllamaChatModel
                .builder()
                .baseUrl(BASE_URL)
                .modelName(MODEL_NAME)
                .temperature(0.0)
                .build();
    }
}
