package com.ITQ.document_service.dto.response;

import com.ITQ.document_service.enums.DocumentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for concurrent approval test summary.
 *
 * @param documentId          the document ID that was processed
 * @param threads             number of threads used
 * @param attempts            total attempts made
 * @param success             count of successful approvals
 * @param conflict            count of conflicts (document not in SUBMITTED)
 * @param notFound            count of not found documents
 * @param registryError       count of registry save errors
 * @param unexpectedError     count of unexpected errors
 * @param finalStatus         final document status after all attempts
 */
@Schema(description = "Concurrent approval test summary")
public record ConcurrentApprovalSummary(
        @Schema(description = "Document ID that was processed", example = "1")
        Long documentId,

        @Schema(description = "Number of threads used", example = "10")
        int threads,

        @Schema(description = "Total attempts made", example = "100")
        int attempts,

        @Schema(description = "Count of successful approvals", example = "1")
        int success,

        @Schema(description = "Count of conflicts (document not in SUBMITTED)", example = "99")
        int conflict,

        @Schema(description = "Count of not found documents", example = "0")
        int notFound,

        @Schema(description = "Count of registry save errors", example = "0")
        int registryError,

        @Schema(description = "Count of unexpected errors", example = "0")
        int unexpectedError,

        @Schema(description = "Final document status after all attempts", example = "APPROVED")
        DocumentStatus finalStatus
) {
}
