package com.example.doc_intel.LongChainChatModel;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

@Component
public class LongChainOpenAIChatModel implements LongChainChatModel {

    private final String OPENAI_API_KEY = "OPENAI_API_KEY";
    private final String MODEL_NAME = "gpt-4o-mini";

    @Override
    public ChatModel giveMeModel() {
        return OpenAiChatModel.builder()
                .apiKey(OPENAI_API_KEY)
                .modelName(MODEL_NAME)
                .build();
    }
}
