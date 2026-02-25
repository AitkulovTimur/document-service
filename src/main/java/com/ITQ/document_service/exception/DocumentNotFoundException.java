package com.ITQ.document_service.exception;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(Long id) {
        super("Document not found with id=" + id);
    }

    public DocumentNotFoundException(String number) {
        super("Document not found with number=" + number);
    }

}