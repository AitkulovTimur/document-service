package com.ITQ.document_service.dto.response;

import com.ITQ.document_service.enums.ApprovalStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for individual document approval result.
 *
 * @param documentId the document ID that was processed
 * @param status     the approval status (SUCCESS, CONFLICT, NOT_FOUND, REGISTRY_ERROR)
 */
@Schema(description = "Individual document approval result")
public record DocumentApprovalResult(
        @Schema(
                description = "Document ID that was processed",
                example = "1"
        )
        Long documentId,

        @Schema(
                description = "Approval status",
                allowableValues = {"SUCCESS", "CONFLICT", "NOT_FOUND", "REGISTRY_ERROR"},
                example = "SUCCESS"
        )
        ApprovalStatus status
) {
}
