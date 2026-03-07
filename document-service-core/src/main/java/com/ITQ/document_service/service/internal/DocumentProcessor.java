package com.ITQ.document_service.service.internal;

import com.ITQ.document_service.dto.request.ApprovalRequest;
import com.ITQ.document_service.dto.request.SubmissionRequest;
import com.ITQ.document_service.dto.response.DocumentApprovalResult;
import com.ITQ.document_service.dto.response.DocumentSubmissionResult;
import com.ITQ.document_service.entity.ApprovalRegistry;
import com.ITQ.document_service.entity.Document;
import com.ITQ.document_service.entity.DocumentHistory;
import com.ITQ.document_service.enums.ApprovalStatus;
import com.ITQ.document_service.enums.DocumentAction;
import com.ITQ.document_service.enums.DocumentStatus;
import com.ITQ.document_service.enums.OperationForLogType;
import com.ITQ.document_service.enums.SubmissionStatus;
import com.ITQ.document_service.repository.ApprovalRegistryRepository;
import com.ITQ.document_service.repository.DocumentHistoryRepository;
import com.ITQ.document_service.repository.DocumentRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Internal service responsible for processing individual document operations.
 *
 * <p>This class handles the core business logic for document lifecycle operations,
 * specifically document submission and approval. It operates with transaction isolation
 * to ensure data consistency and provides detailed logging for audit purposes.</p>
 *
 * <p><strong>Key Responsibilities:</strong></p>
 * <ul>
 *   <li>Processing document submissions from DRAFT to SUBMITTED status</li>
 *   <li>Processing document approvals from SUBMITTED to APPROVED status</li>
 *   <li>Maintaining document history for audit trails</li>
 *   <li>Managing approval registry entries</li>
 *   <li>Handling transaction rollback on errors</li>
 * </ul>
 *
 * <p><strong>Transaction Management:</strong></p>
 * <p>All operations use {@code REQUIRES_NEW} propagation to ensure each document
 * operation runs in its own transaction, providing isolation from batch operations
 * that may call this service.</p>
 *
 * <p><strong>Error Handling:</strong></p>
 * <ul>
 *   <li>Documents not found return appropriate NOT_FOUND status</li>
 *   <li>Invalid status transitions return CONFLICT status</li>
 *   <li>Registry creation failures trigger transaction rollback</li>
 * </ul>
 *
 * @author Aitkulov Tim
 * @see Document
 * @see DocumentHistory
 * @see ApprovalRegistry
 */
@Slf4j
@Service
public class DocumentProcessor {

    private final DocumentRepository documentRepository;
    private final DocumentHistoryRepository documentHistoryRepository;
    private final ApprovalRegistryRepository approvalRegistryRepository;

    private final TransactionTemplate transactionTemplate;

    public DocumentProcessor(
            DocumentRepository documentRepository,
            DocumentHistoryRepository documentHistoryRepository,
            ApprovalRegistryRepository approvalRegistryRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.documentRepository = documentRepository;
        this.documentHistoryRepository = documentHistoryRepository;
        this.approvalRegistryRepository = approvalRegistryRepository;

        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    private static void logHistoryForDocCreation(Document document, String actor) {
        log.debug("{}History entity for document with ID {} will be created. Actor: {}",
                OperationForLogType.SUBMIT_DOCUMENT.getOperation(), document.getId(), actor);
    }

    /**
     * Processes a single document submission request.
     *
     * <p>This method handles the transition of a document from DRAFT to SUBMITTED status.
     * It runs in a new transaction to ensure isolation from other operations.
     * The method validates the document exists and is in the correct status before
     * proceeding with the submission.</p>
     *
     * <p><strong>Process Flow:</strong></p>
     * <ol>
     *   <li>Validate document exists</li>
     *   <li>Check document is in DRAFT status</li>
     *   <li>Update status to SUBMITTED</li>
     *   <li>Create history entry for audit trail</li>
     *   <li>Return submission result</li>
     * </ol>
     *
     * @param request the submission request containing document ID and comment
     * @param actor   the user or system performing the submission
     * @return {@link DocumentSubmissionResult} indicating success or failure reason
     * @throws IllegalArgumentException if request or actor is null
     * @see DocumentStatus#DRAFT
     * @see DocumentStatus#SUBMITTED
     * @see DocumentHistory
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DocumentSubmissionResult submitSingleDocument(@NonNull SubmissionRequest request, @NonNull String actor) {
        Long documentId = request.id();

        log.debug("{}Processing document submission for ID: {}", OperationForLogType.SUBMIT_DOCUMENT.getOperation(), documentId);

        return documentRepository.findById(documentId)
                .map(doc -> processDraft(doc, request.comment(), actor))
                .orElseGet(() -> {
                    log.debug("{}Document with ID {} not found",
                            OperationForLogType.SUBMIT_DOCUMENT.getOperation(), documentId);
                    return new DocumentSubmissionResult(documentId, SubmissionStatus.NOT_FOUND);
                });
    }

    /**
     * Processes a DRAFT document for submission.
     *
     * <p>This private method performs the actual status transition and history creation.
     * It validates that the document is in DRAFT status before proceeding with the submission.</p>
     *
     * @param document the document to process (must be in DRAFT status)
     * @param comment  optional comment for the history entry
     * @param actor    the user performing the submission
     * @return {@link DocumentSubmissionResult} with SUCCESS or CONFLICT status
     */
    private DocumentSubmissionResult processDraft(Document document, String comment, String actor) {
        if (document.getStatus() != DocumentStatus.DRAFT) {
            log.debug("{}Document with ID {} has status {}, cannot submit from DRAFT",
                    OperationForLogType.SUBMIT_DOCUMENT.getOperation(), document.getId(), document.getStatus());
            return new DocumentSubmissionResult(document.getId(), SubmissionStatus.CONFLICT);
        }
        document.setStatus(DocumentStatus.SUBMITTED);

        logHistoryForDocCreation(document, actor);
        DocumentHistory history = DocumentHistory.builder()
                .document(document)
                .actor(actor)
                .comment(comment)
                .action(DocumentAction.SUBMIT)
                .build();

        documentHistoryRepository.save(history);

        log.debug("{}Document with ID {} successfully submitted for approval (DRAFT -> SUBMITTED)",
                OperationForLogType.SUBMIT_DOCUMENT.getOperation(), document.getId());
        return new DocumentSubmissionResult(document.getId(), SubmissionStatus.SUCCESS);
    }

    /**
     * Processes a single document approval request.
     *
     * <p>This method handles the transition of a document from SUBMITTED to APPROVED status.
     * It runs in a new transaction to ensure isolation and performs additional validation
     * by creating an approval registry entry. If registry creation fails, the transaction
     * is rolled back to maintain data consistency.</p>
     *
     * <p><strong>Process Flow:</strong></p>
     * <ol>
     *   <li>Validate document exists</li>
     *   <li>Check document is in SUBMITTED status</li>
     *   <li>Update status to APPROVED</li>
     *   <li>Create history entry for audit trail</li>
     *   <li>Create approval registry entry</li>
     *   <li>Handle registry creation failures with rollback</li>
     *   <li>Return approval result</li>
     * </ol>
     *
     * @param approvalRequest the approval request containing document ID and comment
     * @param approvedBy      the user or system performing the approval
     * @return {@link DocumentApprovalResult} indicating success or failure reason
     * @throws IllegalArgumentException if request or approvedBy is null
     * @see DocumentStatus#SUBMITTED
     * @see DocumentStatus#APPROVED
     * @see ApprovalRegistry
     * @see DocumentHistory
     */
    public DocumentApprovalResult approveSingleDocument(@NonNull ApprovalRequest approvalRequest, @NonNull String approvedBy) {
        return transactionTemplate.execute(status -> {
            Long documentId = approvalRequest.id();
            log.debug("{}Processing document approval for ID: {}. Approved by: {}",
                    OperationForLogType.APPROVE_DOCUMENT.getOperation(), documentId, approvedBy);

            return documentRepository.findById(documentId)
                    .map(document -> processSubmitted(document, approvalRequest.comment(), approvedBy, status))
                    .orElseGet(() -> {
                        log.debug("{}Document with ID {} not found for approval",
                                OperationForLogType.APPROVE_DOCUMENT.getOperation(), documentId);
                        return new DocumentApprovalResult(documentId, ApprovalStatus.NOT_FOUND);
                    });
        });
    }

    /**
     * Processes a SUBMITTED document for approval.
     *
     * <p>This private method performs the actual status transition, history creation,
     * and approval registry entry creation. It includes error handling for registry
     * creation failures with automatic transaction rollback.</p>
     *
     * @param document   the document to process (must be in SUBMITTED status)
     * @param comment    optional comment for the history entry
     * @param approvedBy the user performing the approval
     * @return {@link DocumentApprovalResult} with SUCCESS, CONFLICT, or REGISTRY_ERROR status
     */
    private DocumentApprovalResult processSubmitted(
            @NonNull Document document,
            String comment,
            String approvedBy,
            org.springframework.transaction.TransactionStatus transactionStatus
    ) {

        if (document.getStatus() != DocumentStatus.SUBMITTED) {
            log.debug("{}Document with ID {} has status {}, cannot approve from SUBMITTED",
                    OperationForLogType.APPROVE_DOCUMENT.getOperation(), document.getId(), document.getStatus());
            return new DocumentApprovalResult(document.getId(), ApprovalStatus.CONFLICT);
        }

        try {
            document.setStatus(DocumentStatus.APPROVED);

            logHistoryForDocCreation(document, approvedBy);
            DocumentHistory history = DocumentHistory.builder()
                    .document(document)
                    .actor(approvedBy)
                    .comment(comment)
                    .action(DocumentAction.APPROVE)
                    .build();
            documentHistoryRepository.save(history);


            ApprovalRegistry registry = ApprovalRegistry.builder()
                    .document(document)
                    .approvedBy(approvedBy)
                    .approvedAt(java.time.OffsetDateTime.now())
                    .build();
            approvalRegistryRepository.save(registry);

            documentRepository.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("{} Concurrent modification detected for document ID: {}. Details: {}",
                    OperationForLogType.APPROVE_DOCUMENT.getOperation(), document.getId(), e.getMessage());

            transactionStatus.setRollbackOnly();
            return new DocumentApprovalResult(document.getId(), ApprovalStatus.CONFLICT);

        } catch (Exception e) {
            log.error("{} Registry error for document ID: {}",
                    OperationForLogType.APPROVE_DOCUMENT.getOperation(), document.getId(), e);

            transactionStatus.setRollbackOnly();
            return new DocumentApprovalResult(document.getId(), ApprovalStatus.REGISTRY_ERROR);
        }

        log.debug("{}Document with ID {} successfully approved (SUBMITTED -> APPROVED) and registry entry created",
                OperationForLogType.APPROVE_DOCUMENT.getOperation(), document.getId());
        return new DocumentApprovalResult(document.getId(), ApprovalStatus.SUCCESS);
    }
}
