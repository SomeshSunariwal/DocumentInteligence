package com.example.doc_intel.Exceptions;

import java.util.stream.Collectors;
import java.util.Map;

import com.example.doc_intel.Constants.ErrorCode;
import com.example.doc_intel.DTO.ExceptionDTO;
import com.example.doc_intel.Exceptions.ChatModelExceptions.AIConfigNotExistException;
import com.example.doc_intel.Exceptions.ChatModelExceptions.NoResultFoundException;
import com.example.doc_intel.Exceptions.DBExceptions.DocumentNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalException {


    // Do not need to HttpStatus.INTERNAL_SERVER_ERROR
    private static final Map<Class<? extends CodedRuntimeException>, HttpStatus> HTTP_STATUS_BY_EXCEPTION = Map.of(
        UnAuthenticatedUser.class, HttpStatus.UNAUTHORIZED,
        UnSupportedFileException.class, HttpStatus.BAD_REQUEST,
        NoResultFoundException.class, HttpStatus.NOT_FOUND,
        DocumentNotExistException.class, HttpStatus.NOT_FOUND,
        UserNotExistException.class, HttpStatus.NOT_FOUND,
        AIConfigNotExistException.class, HttpStatus.NOT_FOUND
    );

    @ExceptionHandler(CodedRuntimeException.class)
    public ResponseEntity<ExceptionDTO> handleCodedException(CodedRuntimeException exception) {
        HttpStatus status = getStatus(exception);
        if (status.is5xxServerError()) {
            log.error("Application error code {}", exception.getErrorCode(), exception);
        }
        return ResponseEntity.status(status)
            .body(new ExceptionDTO(exception.getErrorCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionDTO> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException exception) {
        String errors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ExceptionDTO(ErrorCode.InvalidRequest, errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDTO> handleAllExceptions(Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ExceptionDTO(ErrorCode.UnexpectedError, "An unexpected error occurred"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionDTO> handleBadCredentialsExceptions(Exception exception) {
        log.error("Bad Credentials exception", exception);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ExceptionDTO(ErrorCode.BadCredentials, "Email and Password is not correct"));
    }

    private HttpStatus getStatus(CodedRuntimeException exception) {
        return HTTP_STATUS_BY_EXCEPTION.getOrDefault(exception.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
