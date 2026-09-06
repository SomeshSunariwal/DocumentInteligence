package com.example.doc_intel.LongChainChatModel;

import org.springframework.beans.factory.annotation.Autowired;
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

    public LongChainChatModel giveMeChatModel(String name) {
        if (name.equals("ollamaChatModel")) {
            return longChainOllamaChatModel;
        } else if (name.equals("openAIChatModel")) {
            return longChainOpenAIChatModel;
        }
        return longChainLocalAIModel;
    }
}
