package com.ITQ.document_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO for batch document approval result.
 * Contains results for each document ID in the approval request.
 *
 * @param results list of individual document approval results
 */
@Schema(description = "Batch document approval result")
public record ApprovalResult(
        @Schema(
                description = "Results for each document approval attempt",
                example = "[{\"documentId\": 1, \"status\": \"SUCCESS\"}, {\"documentId\": 2, \"status\": \"CONFLICT\"}, {\"documentId\": 3, \"status\": \"NOT_FOUND\"}, {\"documentId\": 4, \"status\": \"REGISTRY_ERROR\"}]"
        )
        List<DocumentApprovalResult> results
) {
}
