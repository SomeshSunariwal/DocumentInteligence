package com.example.doc_intel.dto;

import java.util.List;

public class QuestionResponseDTO {
    List<TextSegmentResponseDTO> textSegmentResponseDTOList;
    String result;

    public QuestionResponseDTO(String result, List<TextSegmentResponseDTO> textSegmentResponseDTOList) {
        this.result = result;
        this.textSegmentResponseDTOList = textSegmentResponseDTOList;
    }

    public List<TextSegmentResponseDTO> getTextSegmentResponseDTOList() {
        return textSegmentResponseDTOList;
    }

    public void setTextSegmentResponseDTOList(List<TextSegmentResponseDTO> textSegmentResponseDTOList) {
        this.textSegmentResponseDTOList = textSegmentResponseDTOList;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
