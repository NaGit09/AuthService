package com.furniro.AuthService.exception;

import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ErrorType;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<AType> handleBaseException(BaseException ex) {

        AType error = ErrorType.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.valueOf(ex.getCode()));
    }

}
