package com.example.doc_intel.Exceptions;

import com.example.doc_intel.DTO.ExceptionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(MessageLengthException.class)
    ResponseEntity<ExceptionDTO> handleMessageLengthException(MessageLengthException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDTO(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    @ExceptionHandler(ProcessFileException.class)
    ResponseEntity<ExceptionDTO> handleProcessFileException(ProcessFileException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    @ExceptionHandler(InternalServerErrorException.class)
    ResponseEntity<ExceptionDTO> handleInternalServerErrorException(InternalServerErrorException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    @ExceptionHandler(NullMessageException.class)
    ResponseEntity<ExceptionDTO> handleNullMessageException(NullMessageException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDTO(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    @ExceptionHandler(NoResultFoundException.class)
    ResponseEntity<ExceptionDTO> handleNoResultFoundException(NoResultFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ExceptionDTO(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler(FileSupportError.class)
    ResponseEntity<ExceptionDTO> handleFileSupportError(FileSupportError e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDTO(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    @ExceptionHandler(FileReadError.class)
    ResponseEntity<ExceptionDTO> handleFileReadError(FileReadError e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    @ExceptionHandler(OpenSearchVectoreException.class)
    ResponseEntity<ExceptionDTO> handleOpenSearchVectoreException(OpenSearchVectoreException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    @ExceptionHandler(OpenSearchIndexingException.class)
    ResponseEntity<ExceptionDTO> handleOpenSearchIndexingException(OpenSearchIndexingException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    @ExceptionHandler(BuckerCreationException.class)
    ResponseEntity<ExceptionDTO> handleBuckerCreationException(BuckerCreationException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    @ExceptionHandler(MinIOObjectPutException.class)
    ResponseEntity<ExceptionDTO> handleMinIOObjectPutException(MinIOObjectPutException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ExceptionDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionDTO(HttpStatus.BAD_GATEWAY.value(), errors));
    }

    @ExceptionHandler(PSQLDBException.class)
    ResponseEntity<ExceptionDTO> handlePSQLDBException(PSQLDBException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

}
