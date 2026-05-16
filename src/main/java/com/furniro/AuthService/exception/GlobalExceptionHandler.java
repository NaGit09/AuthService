package com.furniro.AuthService.exception;

import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ErrorType;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<AType> handleBaseException(CustomException ex) {

        AType error = ErrorType.builder()
                .code(ex.getErrorCode().getCode())
                .message(ex.getErrorCode().getMessage())
                .build();

        return ResponseEntity.status(ex.getErrorCode().getCode())
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AType> handleException(Exception ex) {

        AType error = ErrorType.builder()
                .code(500)
                .message("An unexpected error occurred: " + ex.getMessage())
                .build();

        return ResponseEntity.status(500)
                .body(error);
    }

}
