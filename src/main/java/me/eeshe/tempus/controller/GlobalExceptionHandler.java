package me.eeshe.tempus.controller;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import me.eeshe.tempus.dto.ErrorResponseDTO;
import me.eeshe.tempus.exception.ClientNotFoundException;
import me.eeshe.tempus.exception.GroupNotFoundException;
import me.eeshe.tempus.exception.ProjectNotFoundException;
import me.eeshe.tempus.exception.TaskNotFoundException;
import me.eeshe.tempus.exception.TimeEntryNotFoundException;
import me.eeshe.tempus.exception.UserNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException exception) {
        final String errorMessage = exception.getBindingResult().getFieldErrors().stream().findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).orElse("Validation failed.");
        final ErrorResponseDTO errorResponseDto = new ErrorResponseDTO(errorMessage);

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleGroupNotFoundException(GroupNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(UserNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleClientNotFoundException(ClientNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleClientNotFoundException(ProjectNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleClientNotFoundException(TaskNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TimeEntryNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleClientNotFoundException(TimeEntryNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentialsException(BadCredentialsException exception) {
        return new ResponseEntity<>(new ErrorResponseDTO("Incorrect user or password"), HttpStatus.UNAUTHORIZED);
    }
}
