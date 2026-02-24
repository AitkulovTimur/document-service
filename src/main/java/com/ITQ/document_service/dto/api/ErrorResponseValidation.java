package com.ITQ.document_service.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class ErrorResponseValidation extends ErrorResponse {

    private final List<FieldError> errors;

    public ErrorResponseValidation(String message, List<FieldError> errors) {
        super(HttpStatus.BAD_REQUEST.name(), message);
        this.errors = errors;
    }

    public record FieldError(String field, String rejectedValue, String error) {
    }
}