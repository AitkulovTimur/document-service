package com.ITQ.document_service.controller.handler;

import com.ITQ.document_service.dto.api.ErrorResponse;
import com.ITQ.document_service.dto.api.ErrorResponseValidation;
import com.ITQ.document_service.enums.OperationForLogType;
import com.ITQ.document_service.exception.DocumentNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the document service.
 * Handles validation exceptions and other application-wide errors.
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ObjectMapper objectMapper;

    /**
     * Handles validation exceptions that occur when request body fails validation.
     *
     * @param ex the MethodArgumentNotValidException containing validation errors
     * @return ResponseEntity with error details and HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        List<ErrorResponseValidation.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ErrorResponseValidation.FieldError(
                        err.getField(),
                        err.getRejectedValue() != null ? err.getRejectedValue().toString() : null,
                        err.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ErrorResponse body = new ErrorResponseValidation(
                "Validation failed",
                fieldErrors
        );

        tryLogWithJsonStringOrUseException("Validation failed. Details:",
                OperationForLogType.CREATE_DOCUMENT, fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * Handles DocumentNotFoundException when a document is not found.
     *
     * @param ex the DocumentNotFoundException containing error details
     * @return ResponseEntity with error details and HTTP 404 status
     */
    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentNotFound(DocumentNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.NOT_FOUND.name(),
                ex.getMessage()
        );

        log.error("{}{}", OperationForLogType.GET_DOCUMENT.getOperation(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    private void tryLogWithJsonStringOrUseException(String exceptionDescription, OperationForLogType operation,
                                                    Object objToJsonfy) {
        try {
            log.error("{}{}\n{}", operation.getOperation(), exceptionDescription, objectMapper.writeValueAsString(objToJsonfy));
        } catch (JsonProcessingException e) {
            log.error("{}{}", operation.getOperation(), exceptionDescription, e);
        }

    }
}