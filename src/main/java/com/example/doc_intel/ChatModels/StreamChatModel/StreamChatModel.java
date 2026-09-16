package com.example.doc_intel.ChatModels.StreamChatModel;

import com.example.doc_intel.Entity.AIConfig;
import dev.langchain4j.model.chat.StreamingChatModel;

public interface StreamChatModel {
    StreamingChatModel giveMeModel(AIConfig aiConfig);
}
