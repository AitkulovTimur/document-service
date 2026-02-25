package com.ITQ.document_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for create document response.
 * Used when creating a new document or when history is not required.
 *
 * @param documentInfo — core document information including id, number, author, title, status, and timestamps
 */
@Schema(description = "Create document response DTO")
public record CreateDocumentResponse(
        @Schema(
                description = "Core document information",
                implementation = DocumentInfo.class
        )
        DocumentInfo documentInfo
) {
}
