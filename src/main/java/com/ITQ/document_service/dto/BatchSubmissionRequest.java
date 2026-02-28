package com.ITQ.document_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * DTO for batch document submission request.
 * Contains list of document IDs to submit for approval.
 *
 * @param idsWithComments list of document IDs to submit (1-1000) with comments
 * @param actor           actor who submitted the documents
 */
@Schema(description = "Batch document submission request")
public record BatchSubmissionRequest(
        @Schema(
                description = "List of document IDs to submit for approval",
                example = "[1, 2, 3, 4, 5]"
        )
        @NotEmpty(message = "IDs list cannot be empty")
        @Size(max = 1000, message = "Cannot process more than 1000 documents at once")
        Set<SubmissionRequest> idsWithComments,

        @Size(max = 255, message = "actor must be at most 255 characters")
        @NotBlank(message = "Actor must not be empty")
        String actor
) {
}
