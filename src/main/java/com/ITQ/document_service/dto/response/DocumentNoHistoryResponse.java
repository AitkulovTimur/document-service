package com.ITQ.document_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for document response.
 * Used when creating a new document or when history is not required.
 *
 * @param documentInfo — core document information including id, number, author, title, status, and timestamps
 */
@Schema(description = "Document response DTO (no history)")
public record DocumentNoHistoryResponse(
        @Schema(
                description = "Core document information",
                implementation = DocumentInfo.class
        )
        DocumentInfo documentInfo
) {
}
