package me.eeshe.tempus.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import me.eeshe.tempus.dto.ErrorResponseDTO;
import me.eeshe.tempus.exception.GroupNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleGroupNotFoundException(GroupNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }
}
