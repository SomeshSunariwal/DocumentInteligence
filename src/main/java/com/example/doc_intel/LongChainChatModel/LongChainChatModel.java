package com.example.doc_intel.LongChainChatModel;

import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Component;

@Component
public interface LongChainChatModel {
    ChatModel giveMeModel();
}
