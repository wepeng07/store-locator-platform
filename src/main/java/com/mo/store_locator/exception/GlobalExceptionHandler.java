package com.mo.store_locator.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.mo.store_locator.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return buildErrorResponse(status, ex.getReason() != null ? ex.getReason() : status.getReasonPhrase(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .forEach(errors::add);
        ex.getBindingResult().getGlobalErrors().stream()
                .map(this::formatObjectError)
                .forEach(errors::add);

        String message = errors.isEmpty() ? "Validation failed" : String.join("; ", errors);

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
        List<String> errors = new ArrayList<>();

        for (ParameterValidationResult validationResult : ex.getParameterValidationResults()) {
            if (validationResult instanceof ParameterErrors parameterErrors) {
                parameterErrors.getFieldErrors().stream()
                        .map(this::formatFieldError)
                        .forEach(errors::add);
                parameterErrors.getGlobalErrors().stream()
                        .map(this::formatObjectError)
                        .forEach(errors::add);
                continue;
            }

            String parameterName = validationResult.getMethodParameter().getParameterName();
            validationResult.getResolvableErrors().stream()
                    .map(error -> formatParameterError(parameterName, error))
                    .forEach(errors::add);
        }

        ex.getCrossParameterValidationResults().stream()
                .map(this::formatMessageSourceResolvable)
                .forEach(errors::add);

        String message = errors.isEmpty() ? "Validation failed" : String.join("; ", errors);

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String property = extractLeafProperty(violation.getPropertyPath().toString());
                    return property == null ? violation.getMessage() : property + ": " + violation.getMessage();
                })
                .collect(Collectors.joining("; "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getParameterName() + ": is required",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid value for parameter '" + ex.getName() + "'",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "Request conflicts with existing data";
        if (containsStoreIdConflict(ex)) {
            message = "Store with the same storeId already exists";
        }

        return buildErrorResponse(HttpStatus.CONFLICT, message, request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        Throwable cause = ex.getMostSpecificCause();
        String message = "Malformed JSON request";

        if (cause instanceof UnrecognizedPropertyException propertyException) {
            message = "Field '" + propertyException.getPropertyName() + "' is not allowed";
        } else if (cause instanceof InvalidFormatException formatException && !formatException.getPath().isEmpty()) {
            message = "Invalid value for field '" + formatJacksonPath(formatException) + "'";
        }

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.name(),
                message,
                path
        ));
    }

    private String formatFieldError(FieldError fieldError) {
        String leafField = extractLeafProperty(fieldError.getField());
        if (isSyntheticValidationField(leafField)) {
            return fieldError.getDefaultMessage();
        }
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private String formatObjectError(ObjectError objectError) {
        return objectError.getDefaultMessage();
    }

    private String formatParameterError(String parameterName, MessageSourceResolvable error) {
        String message = formatMessageSourceResolvable(error);
        if (parameterName == null || parameterName.isBlank()) {
            return message;
        }
        return parameterName + ": " + message;
    }

    private String formatMessageSourceResolvable(MessageSourceResolvable error) {
        return error.getDefaultMessage() == null ? "Validation failed" : error.getDefaultMessage();
    }

    private String extractLeafProperty(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return null;
        }

        int lastDotIndex = propertyPath.lastIndexOf('.');
        return lastDotIndex >= 0 ? propertyPath.substring(lastDotIndex + 1) : propertyPath;
    }

    private boolean isSyntheticValidationField(String fieldName) {
        return "coordinatePair".equals(fieldName)
                || "anyUpdatableField".equals(fieldName)
                || "anyValue".equals(fieldName);
    }

    private String formatJacksonPath(InvalidFormatException exception) {
        return exception.getPath().stream()
                .map(reference -> reference.getFieldName())
                .collect(Collectors.joining("."));
    }

    private boolean containsStoreIdConflict(DataIntegrityViolationException ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase().contains("store_id")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
