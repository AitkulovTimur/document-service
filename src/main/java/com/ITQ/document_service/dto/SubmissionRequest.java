package com.ITQ.document_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Document submission request")
public record SubmissionRequest(
        @Schema(
        )
        @NotNull(message = "ID cannot be null")
        @Min(value = 1, message = "ID must be positive")
        Long id,
        @Size(max = 500, message = "comment must be at most 500 characters")
        String comment
) {
}