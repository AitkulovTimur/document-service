package com.ITQ.document_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * DTO containing common document information.
 * This record encapsulates the core document data that is shared
 * across different response types.
 *
 * @param id        — document identifier
 * @param number    — document number
 * @param author    — document author
 * @param title     — document title
 * @param status    — document status
 * @param createdAt — document creation date
 * @param updatedAt — document last update date
 */
@Schema(description = "Core document information")
public record DocumentInfo(
        @Schema(
                description = "Document identifier",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Document number",
                example = "DOC—hx_Pa14_e"
        )
        String number,

        @Schema(
                description = "Document author",
                example = "Иван Иванов"
        )
        String author,

        @Schema(
                description = "Document title",
                example = "Договор поставки"
        )
        String title,

        @Schema(
                description = "Document status",
                example = "DRAFT",
                allowableValues = {"DRAFT", "PUBLISHED", "ARCHIVED"}
        )
        String status,

        @Schema(
                description = "Document creation date",
                example = "2024-02-24T10:30:00Z"
        )
        OffsetDateTime createdAt,

        @Schema(
                description = "Document last update date",
                example = "2024-02-24T11:45:00Z"
        )
        OffsetDateTime updatedAt
) {
}