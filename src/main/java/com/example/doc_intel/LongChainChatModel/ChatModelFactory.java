package com.example.doc_intel.LongChainChatModel;

import com.example.doc_intel.Enums.ChatModelType;
import org.springframework.stereotype.Component;

@Component
public class ChatModelFactory {

    private final LongChainOllamaChatModel longChainOllamaChatModel;

    private final LongChainOpenAIChatModel longChainOpenAIChatModel;

    private final LongChainLocalAIModel longChainLocalAIModel;

    public ChatModelFactory(LongChainOpenAIChatModel longChainOpenAIChatModel,
                            LongChainOllamaChatModel longChainOllamaChatModel,
                            LongChainLocalAIModel longChainLocalAIModel) {
        this.longChainOllamaChatModel = longChainOllamaChatModel;
        this.longChainOpenAIChatModel = longChainOpenAIChatModel;
        this.longChainLocalAIModel = longChainLocalAIModel;
    }

    public LongChainChatModel giveMeChatModel(ChatModelType type) {
        if (ChatModelType.OLLAMA.equals(type)) {
            return longChainOllamaChatModel;
        } else if (ChatModelType.OPENAI.equals(type)) {
            return longChainOpenAIChatModel;
        }
        return longChainLocalAIModel;
    }
}
