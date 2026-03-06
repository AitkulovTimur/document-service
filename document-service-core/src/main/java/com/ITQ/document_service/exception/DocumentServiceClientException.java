package com.ITQ.document_service.exception;

import com.ITQ.document_service.dto.api.ErrorResponseValidation;
import lombok.Getter;

import java.util.List;

@Getter
public class DocumentServiceClientException extends RuntimeException {
    private List<ErrorResponseValidation.FieldError> clientErrors;

    public DocumentServiceClientException(String message) {
        super(message);
    }

    public DocumentServiceClientException(String message, List<ErrorResponseValidation.FieldError> clientErrors) {
        super(message);
        this.clientErrors = clientErrors;
    }
}
