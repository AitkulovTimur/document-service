package com.ITQ.document_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new document
 *
 * @param author — document author
 * @param title  — document title
 */
@Schema(description = "Запрос на создание нового документа")
public record CreateDocumentRequest(
        @Schema(
                description = "Document author",
                example = "Иван Иванов",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Author must not be empty")
        @Size(max = 255, message = "Author must be at most 255 characters")
        String author,

        @Schema(
                description = "Document title",
                example = "Договор поставки",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Title must not be empty")
        @Size(max = 500, message = "Title must be at most 500 characters")
        String title
) {
}