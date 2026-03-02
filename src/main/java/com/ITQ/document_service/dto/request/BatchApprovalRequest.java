package com.ITQ.document_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * DTO for batch document approval request.
 * Contains list of document IDs (with comments) to approve.
 *
 * @param idsWithComments list of document IDs to approve (1-1000) with comments
 * @param actor           actor who is approving the documents
 */
@Schema(description = "Batch document approval request")
public record BatchApprovalRequest(
        @Schema(
                description = "List of document IDs to approve with comments"
        )
        @NotEmpty(message = "IDs list cannot be empty")
        @Size(max = 1000, message = "Cannot process more than 1000 documents at once")
        Set<ApprovalRequest> idsWithComments,

        @Size(max = 255, message = "Actor must be at most 255 characters")
        @NotBlank(message = "Actor must not be empty")
        String actor
) implements BatchRequest <ApprovalRequest> {
}
