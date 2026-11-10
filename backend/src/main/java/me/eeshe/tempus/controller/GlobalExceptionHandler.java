package me.eeshe.tempus.controller;

import java.util.Currency;
import java.util.stream.Stream;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import me.eeshe.tempus.dto.ErrorResponseDTO;
import me.eeshe.tempus.dto.PatchUserSettingsRequestDTO;
import me.eeshe.tempus.exception.ClientNotFoundException;
import me.eeshe.tempus.exception.InvalidZoneIdException;
import me.eeshe.tempus.exception.ProjectNotFoundException;
import me.eeshe.tempus.exception.StartTimeAfterEndTimeException;
import me.eeshe.tempus.exception.TaskDoesNotBelongToProjectException;
import me.eeshe.tempus.exception.TaskNotFoundException;
import me.eeshe.tempus.exception.TimeEntriesNotFoundException;
import me.eeshe.tempus.exception.TimeEntryNotFoundException;
import me.eeshe.tempus.exception.UserClientAlreadyExistsException;
import me.eeshe.tempus.exception.UserNotFoundException;
import me.eeshe.tempus.exception.UserProjectAlreadyExistsException;
import me.eeshe.tempus.exception.UserProjectTaskAlreadyExistsException;
import me.eeshe.tempus.exception.UsernameAlreadyUsedException;
import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String ERROR_MESSAGE_MALFORMED_BODY = "Malformed request body";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException exception) {
        final BindingResult bindingResult = exception.getBindingResult();
        final String errorMessage = Stream.concat(
                bindingResult.getFieldErrors().stream(),
                bindingResult.getGlobalErrors().stream())
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Validation failed.");
        final ErrorResponseDTO errorResponseDto = new ErrorResponseDTO(errorMessage);

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception) {
        if (isInvalidCurrencyException(exception)) {
            return new ResponseEntity<>(
                    new ErrorResponseDTO(PatchUserSettingsRequestDTO.ERROR_MESSAGE_INVALID_CURRENCY),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(
                new ErrorResponseDTO(ERROR_MESSAGE_MALFORMED_BODY),
                HttpStatus.BAD_REQUEST);
    }

    private static boolean isInvalidCurrencyException(HttpMessageNotReadableException exception) {
        Throwable cause = exception.getCause();
        while (cause != null) {
            if (cause instanceof InvalidFormatException invalidFormatException
                    && Currency.class.equals(invalidFormatException.getTargetType())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(UserNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.NOT_FOUND);
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
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleProjectNotFoundException(ProjectNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleTaskNotFoundException(TaskNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TimeEntryNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleTimeEntryNotFoundException(TimeEntryNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TimeEntriesNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleTimeEntriesNotFoundException(TimeEntriesNotFoundException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.NOT_FOUND);
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

    @ExceptionHandler(TaskDoesNotBelongToProjectException.class)
    public ResponseEntity<ErrorResponseDTO> handleTaskDoesNotBelongToProjectException(
            TaskDoesNotBelongToProjectException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StartTimeAfterEndTimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleStartTimeAfterEndTimeException(
            StartTimeAfterEndTimeException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidZoneIdException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidZoneIdException(InvalidZoneIdException exception) {
        return new ResponseEntity<>(
                new ErrorResponseDTO(exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }
}
