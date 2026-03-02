package com.ITQ.document_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Document submission request")
public record SubmissionRequest(
        @Schema(description = "Document ID to submit", example = "123")
        @NotNull(message = "ID cannot be null")
        @Min(value = 1, message = "ID must be positive")
        Long id,
        @Schema(description = "Optional submission comment", example = "Submitted by support desk team")
        @Size(max = 500, message = "comment must be at most 500 characters")
        String comment
) implements HasId {
}