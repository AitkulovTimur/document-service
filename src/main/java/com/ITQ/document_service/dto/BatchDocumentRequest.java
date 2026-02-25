package com.ITQ.document_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO for batch document retrieval request.
 * Contains list of document IDs.
 *
 * @param ids list of document IDs to retrieve
 */
@Schema(description = "Batch document retrieval request")
public record BatchDocumentRequest(
        @Schema(
                description = "List of document IDs to retrieve",
                example = "[1, 2, 3, 4, 5]"
        )
        @NotEmpty(message = "IDs list cannot be empty")
        List<@NotNull Long> ids
) {
}
