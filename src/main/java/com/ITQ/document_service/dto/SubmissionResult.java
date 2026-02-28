package com.ITQ.document_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO for batch document submission result.
 * Contains results for each document ID in the submission request.
 *
 * @param results list of individual document submission results
 */
@Schema(description = "Batch document submission result")
public record SubmissionResult(
        @Schema(
                description = "Results for each document submission attempt",
                example = "[{\"documentId\": 1, \"status\": \"SUCCESS\"}, {\"documentId\": 2, \"status\": \"CONFLICT\"}, {\"documentId\": 3, \"status\": \"NOT_FOUND\"}]"
        )
        List<DocumentSubmissionResult> results
) {
}

