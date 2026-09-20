package com.example.doc_intel.DTO.ChatModel;

import com.example.doc_intel.DTO.TextSegmentResponseDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ChatStreamResponse {

    private String type;

    private String data;

    private Boolean success;

    private Boolean error;

    private List<TextSegmentResponseDTO> textSegmentResponseDTO;
}