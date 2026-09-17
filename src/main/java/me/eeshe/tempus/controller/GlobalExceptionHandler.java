package me.eeshe.tempus.controller;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import me.eeshe.tempus.dto.ErrorResponseDTO;
import me.eeshe.tempus.exception.ClientNotFoundException;
import me.eeshe.tempus.exception.ProjectNotFoundException;
import me.eeshe.tempus.exception.TaskNotFoundException;
import me.eeshe.tempus.exception.TimeEntryNotFoundException;
import me.eeshe.tempus.exception.UserClientAlreadyExistsException;
import me.eeshe.tempus.exception.UserNotFoundException;
import me.eeshe.tempus.exception.UserProjectAlreadyExistsException;
import me.eeshe.tempus.exception.UserProjectTaskAlreadyExistsException;
import me.eeshe.tempus.exception.UsernameAlreadyUsedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException exception) {
        final String errorMessage = exception.getBindingResult().getFieldErrors().stream().findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).orElse("Validation failed.");
        final ErrorResponseDTO errorResponseDto = new ErrorResponseDTO(errorMessage);

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(UserNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameAlreadyUsedException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsernameAlreadyUsedException(UsernameAlreadyUsedException exception) {
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
    public ResponseEntity<ErrorResponseDTO> handleProjectNotFoundException(ProjectNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleTaskNotFoundException(TaskNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TimeEntryNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleTimeEntryNotFoundException(TimeEntryNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserProjectAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserProjectAlreadyExistsException(
            UserProjectAlreadyExistsException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserProjectTaskAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserProjectTaskAlreadyExistsException(
            UserProjectTaskAlreadyExistsException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserClientAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserClientAlreadyExistsException(
            UserClientAlreadyExistsException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }
}
