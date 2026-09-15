package com.example.doc_intel.Service;

import com.example.doc_intel.ChatModels.StreamChatModel.StreamChatModelClient;
import com.example.doc_intel.Entity.AIConfig;
import com.example.doc_intel.Exceptions.UnAuthenticatedUser;
import com.example.doc_intel.Repository.AIConfigRepository;
import com.example.doc_intel.Utils.Utils;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.Optional;

@Component
@AllArgsConstructor
public class ChatService {

    private final StreamChatModelClient streamChatModelClient;

    private final AIConfigRepository aiConfigRepository;

    public ResponseBodyEmitter chat(@NonNull String question) {
        String email = Utils.getUserEmail();
        Optional<AIConfig> aiConfigOptional =
                aiConfigRepository.findByUser_Email(email);

        if (aiConfigOptional.isEmpty()) {
            throw new UnAuthenticatedUser("No Config Found");
        }

        AIConfig aiConfig = aiConfigOptional.get();

        StreamingChatModel chatModel =
                streamChatModelClient.giveMeModel(aiConfig);

        ResponseBodyEmitter emitter = new ResponseBodyEmitter(10 * 60 * 1000L);

        chatModel.chat(question, new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        try {
                            emitter.send(partialResponse);
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse response) {
                        emitter.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        emitter.completeWithError(error);
                    }
                }
        );

        return emitter;
    }
}
