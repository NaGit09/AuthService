package com.furniro.AuthService.exception;

import com.furniro.AuthService.dto.API.AType;
import com.furniro.AuthService.dto.API.ErrorType;
import com.furniro.AuthService.util.enums.AddressErrorCode;
import com.furniro.AuthService.util.enums.AuthErrorCode;
import com.furniro.AuthService.util.enums.UserErrorCode;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<AType> handleUserException(AuthException ex) {

        AuthErrorCode code = ex.getErrorCode();

        AType error = ErrorType.builder()
                .code(code.getCode())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.valueOf(code.getCode()));
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<AType> handleUserException(UserException ex) {

        UserErrorCode code = ex.getErrorCode();

        AType error = ErrorType.builder()
                .code(code.getCode())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.valueOf(code.getCode()));
    }

    @ExceptionHandler(AddressException.class)
    public ResponseEntity<AType> handleAddressException(AddressException ex) {

        AddressErrorCode code = ex.getErrorCode();

        AType error = ErrorType.builder()
                .code(code.getCode())
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.valueOf(code.getCode()));
    }
}
