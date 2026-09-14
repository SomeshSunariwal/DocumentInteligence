package com.example.doc_intel.LongChainChatModel;

import com.example.doc_intel.Entity.AIConfig;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Component;

@Component
public interface LongChainChatModel {
    ChatModel giveMeModel(AIConfig aiConfig);
}
