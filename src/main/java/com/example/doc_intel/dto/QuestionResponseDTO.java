package com.example.doc_intel.dto;


public class QuestionResponseDTO {
    Double score;
    String result;

    public QuestionResponseDTO(String result, Double score) {
        this.result = result;
        this.score = score;
    }


    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
