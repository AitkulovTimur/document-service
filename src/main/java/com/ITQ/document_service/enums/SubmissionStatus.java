package com.ITQ.document_service.enums;

/**
 * Enum for document submission status.
 */
public enum SubmissionStatus {
    /**
     * Document successfully submitted (DRAFT -> SUBMITTED)
     */
    SUCCESS,

    /**
     * Document found but status transition not allowed (conflict)
     */
    CONFLICT,

    /**
     * Document not found
     */
    NOT_FOUND
}
