package com.example.doc_intel.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(MessageLengthException.class)
    ResponseEntity<ExceptionDTO> handleMessageLengthException(MessageLengthException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ProcessFileException.class)
    ResponseEntity<ExceptionDTO> handleProcessFileException(ProcessFileException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(InternalServerErrorException.class)
    ResponseEntity<ExceptionDTO> handleInternalServerErrorException(InternalServerErrorException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(NullMessageException.class)
    ResponseEntity<ExceptionDTO> handleNullMessageException(NullMessageException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(NoResultFoundException.class)
    ResponseEntity<ExceptionDTO> handleNoResultFoundException(NoResultFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ExceptionDTO(e.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(FileSupportError.class)
    ResponseEntity<ExceptionDTO> handleFileSupportError(FileSupportError e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(FileReadError.class)
    ResponseEntity<ExceptionDTO> handleFileReadError(FileReadError e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(OpenSearchVectoreException.class)
    ResponseEntity<ExceptionDTO> handleOpenSearchVectoreException(OpenSearchVectoreException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(OpenSearchIndexingException.class)
    ResponseEntity<ExceptionDTO> handleOpenSearchIndexingException(OpenSearchIndexingException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
