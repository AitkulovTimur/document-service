package com.ITQ.document_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO for complete document response.
 * Contains document information along with its full history.
 * Used when retrieving a document with all historical changes.
 *
 * @param documentInfo — core document information including id, number, author, title, status, and timestamps
 * @param history      — list of document history entries showing all changes and actions performed
 */
@Schema(description = "Document response DTO")
public record DocumentResponse(
        @Schema(
                description = "Core document information",
                implementation = DocumentInfo.class
        )
        DocumentInfo documentInfo,

        @Schema(
                description = "Document history",
                example = "[]"
        )
        List<DocumentHistoryResponse> history
) {
}