package com.ITQ.document_service.dto;

import com.ITQ.document_service.enums.SubmissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for individual document submission result.
 *
 * @param documentId the document ID that was processed
 * @param status     the submission status (SUCCESS, CONFLICT, NOT_FOUND)
 */
@Schema(description = "Individual document submission result")
public record DocumentSubmissionResult(
        @Schema(
                description = "Document ID that was processed",
                example = "1"
        )
        Long documentId,

        @Schema(
                description = "Submission status",
                allowableValues = {"SUCCESS", "CONFLICT", "NOT_FOUND"},
                example = "SUCCESS"
        )
        SubmissionStatus status
) {
}
