package com.ITQ.document_service.enums;

/**
 * Enum for document approval status.
 */
public enum ApprovalStatus {
    /**
     * Document successfully approved (SUBMITTED -> APPROVED)
     */
    SUCCESS,

    /**
     * Document found but status transition not allowed (conflict)
     */
    CONFLICT,

    /**
     * Document not found
     */
    NOT_FOUND,

    /**
     * Failed to create registry entry (approval rolled back)
     */
    REGISTRY_ERROR
}
