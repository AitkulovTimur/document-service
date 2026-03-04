package com.ITQ.document_service.service;

import com.ITQ.document_service.dto.response.ConcurrentApprovalSummary;

/**
 * Service responsible for running concurrent operations.
 */
public interface ConcurrentDocumentService {
    /**
     * Executes multiple parallel attempts to approve the same document.
     *
     * @param documentId document id to approve
     * @param threads    number of threads in a pool
     * @param attempts   number of approval attempts to run
     * @param actor      actor name used for all attempts
     * @return summary of the execution (counts by status and final document status)
     */
    ConcurrentApprovalSummary testConcurrentApproval(Long documentId, int threads, int attempts, String actor);
}
