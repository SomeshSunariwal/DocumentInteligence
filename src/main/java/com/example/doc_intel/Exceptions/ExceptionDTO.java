package com.example.doc_intel.Exceptions;

import org.springframework.http.HttpStatus;

public class ExceptionDTO {
    String message;
    Integer code;

    public ExceptionDTO(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
