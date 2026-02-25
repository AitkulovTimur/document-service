package com.ITQ.document_service.service;

import com.ITQ.document_service.dto.CreateDocumentRequest;
import com.ITQ.document_service.dto.CreateDocumentResponse;
import com.ITQ.document_service.dto.DocumentResponse;

/**
 * Service interface for managing document operations.
 *
 * <p>This interface provides business logic for document creation and management
 * within the document service application. It defines the contract for document
 * processing operations.</p>
 *
 * @version 1.0
 * @since 1.0
 */
public interface DocumentService {

    /**
     * Creates a new document based on the provided request.
     *
     * <p>This method processes the document creation request
     * and returns a response containing the created document details including
     * generated ID, number, author, status, title and timestamps.</p>
     *
     * @param request the document creation request containing author and title information
     * @return a {@link CreateDocumentResponse} containing the created document details
     */
    CreateDocumentResponse create(CreateDocumentRequest request);

    /**
     * Finds a document by its unique identifier.
     *
     * @param id the document identifier
     * @return a {@link DocumentResponse} containing the document details
     * @throws com.ITQ.document_service.exception.DocumentNotFoundException if document not found
     */
    DocumentResponse findById(Long id);

    /**
     * Finds a document by its unique number.
     *
     * @param number the document number
     * @return a {@link DocumentResponse} containing the document details
     * @throws com.ITQ.document_service.exception.DocumentNotFoundException if document not found
     */
    DocumentResponse findByNumber(String number);
}
