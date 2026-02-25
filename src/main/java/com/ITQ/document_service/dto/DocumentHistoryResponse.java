package com.ITQ.document_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * DTO for document history response.
 *
 * @param id       — history entry identifier
 * @param action   — performed action
 * @param actor    — who performed the action
 * @param comment  — action comment
 * @param createdAt — history entry creation date
 */
@Schema(description = "Document history response DTO")
public record DocumentHistoryResponse(
        @Schema(
                description = "History entry identifier",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Performed action",
                example = "CREATED"
        )
        String action,

        @Schema(
                description = "Who performed the action",
                example = "admin"
        )
        String actor,

        @Schema(
                description = "Action comment",
                example = "Document created"
        )
        String comment,

        @Schema(
                description = "History entry creation date",
                example = "2024-02-24T10:30:00Z"
        )
        OffsetDateTime createdAt
) {
}
