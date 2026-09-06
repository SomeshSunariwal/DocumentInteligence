package com.example.doc_intel.dto;

public class TextSegmentResponseDTO {
    private String fileName;
    private Integer pageNumber;
    private Integer lineNumber;
    private String score;
    private String text;

    public TextSegmentResponseDTO(String fileName, Integer pageNumber,
                                  Integer lineNumber, String text, String score) {
        this.fileName = fileName;
        this.pageNumber = pageNumber;
        this.lineNumber = lineNumber;
        this.score = score;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
