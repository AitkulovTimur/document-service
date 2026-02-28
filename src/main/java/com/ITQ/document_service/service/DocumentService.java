package com.ITQ.document_service.service;

import com.ITQ.document_service.dto.BatchDocumentRequest;
import com.ITQ.document_service.dto.BatchSubmissionRequest;
import com.ITQ.document_service.dto.CreateDocumentRequest;
import com.ITQ.document_service.dto.DocumentNoHistoryResponse;
import com.ITQ.document_service.dto.DocumentResponse;
import com.ITQ.document_service.dto.SubmissionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * @return a {@link DocumentNoHistoryResponse} containing the created document details
     */
    DocumentNoHistoryResponse create(CreateDocumentRequest request);

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

    /**
     * Retrieves documents by their IDs with pagination and sorting.
     *
     * @param request the batch document request containing IDs
     * @param pageable pagination and sorting parameters
     * @return a {@link Page} of DocumentResponse objects
     */
    Page<DocumentResponse> findByIdIn(BatchDocumentRequest request, Pageable pageable);

    /**
     * Submits documents for approval in batch.
     *
     * <p>This method attempts to transition each document from DRAFT to SUBMITTED status.
     * Processing is atomic for each document, and partial successes are allowed.
     * Results are returned for each document ID indicating success, conflict, or not found.</p>
     *
     * @param request the batch submission request containing document IDs
     * @return a {@link SubmissionResult} containing results for each document
     */
    SubmissionResult submitDocuments(BatchSubmissionRequest request);
}
