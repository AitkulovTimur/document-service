package com.ITQ.document_service.dto.request;

import com.ITQ.document_service.enums.DocumentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;

import java.time.OffsetDateTime;

/**
 * DTO for document search request with filtering parameters.
 *
 * <p>This class encapsulates all possible filters for document search operations.
 * All parameters are optional - if not provided, the corresponding filter will not be applied.</p>
 *
 * @param status   Optional document status filter
 * @param author   Optional author filter (case-insensitive partial match)
 * @param dateFrom Optional start date for creation date range (inclusive)
 * @param dateTo   Optional end date for creation date range (inclusive)
 */
@Schema(description = "Document search request with optional filters")
public record DocumentSearchRequest(

        @Schema(
                description = "Document status filter",
                example = "SUBMITTED"
        )
        DocumentStatus status,

        @Schema(
                description = "Author filter (case-insensitive partial match)",
                example = "john.doe"
        )
        String author,

        @Schema(
                description = "Start date for creation date range (inclusive)",
                example = "2024-01-01T00:00:00Z"
        )
        @PastOrPresent(message = "Date from must be in the past or present")
        OffsetDateTime dateFrom,

        @Schema(
                description = "End date for creation date range (inclusive)",
                example = "2024-12-31T23:59:59Z"
        )
        @PastOrPresent(message = "Date to must be in the past or present")
        OffsetDateTime dateTo
) {

    /**
     * Validates that dateFrom is not after dateTo if both are provided.
     *
     * @return true if the date range is valid, false otherwise
     */
    public boolean isValidDateRange() {
        if (dateFrom != null && dateTo != null) {
            return !dateFrom.isAfter(dateTo);
        }
        return true;
    }
}
