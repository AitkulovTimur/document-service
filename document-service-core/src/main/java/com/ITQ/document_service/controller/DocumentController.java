package com.ITQ.document_service.controller;

import com.ITQ.document_service.dto.request.BatchApprovalRequest;
import com.ITQ.document_service.dto.request.BatchDocumentRequest;
import com.ITQ.document_service.dto.request.BatchSubmissionRequest;
import com.ITQ.document_service.dto.request.CreateDocumentRequest;
import com.ITQ.document_service.dto.request.DocumentSearchRequest;
import com.ITQ.document_service.dto.response.ApprovalResult;
import com.ITQ.document_service.dto.response.DocumentInfo;
import com.ITQ.document_service.dto.response.DocumentNoHistoryResponse;
import com.ITQ.document_service.dto.response.DocumentResponse;
import com.ITQ.document_service.dto.response.SubmissionResult;
import com.ITQ.document_service.enums.DocumentStatus;
import com.ITQ.document_service.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document management API")
public class DocumentController {
    private final DocumentService documentService;

    @Operation(summary = "Create document",
            description = "Creates a new document in DRAFT status")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Document created",
                    content = @Content(schema = @Schema(implementation = DocumentNoHistoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentNoHistoryResponse create(@Valid @RequestBody CreateDocumentRequest request) {
        return documentService.create(request);
    }

    @Operation(summary = "Get document by ID",
            description = "Retrieves a document by its unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document found",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/{id}")
    public DocumentResponse findById(
            @Parameter(
                    name = "id",
                    description = "Unique identifier of the document",
                    example = "1",
                    required = true
            )
            @PathVariable(name = "id") Long id
    ) {
        return documentService.findById(id);
    }

    @Operation(summary = "Get document by number",
            description = "Retrieves a document by its unique number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document found",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/by-number/{number}")
    public DocumentResponse findByNumber(
            @Parameter(
                    name = "number",
                    description = "Business number of the document",
                    example = "DOC-2024-001",
                    required = true
            )
            @PathVariable(name = "number") String number
    ) {
        return documentService.findByNumber(number);
    }

    @Operation(summary = "Get documents by IDs (batch)",
            description = "Retrieves documents by their IDs with pagination and sorting")
    @PostMapping("/batch")
    public Page<DocumentResponse> findByIdIn(
            @Valid @RequestBody BatchDocumentRequest request,
            @ParameterObject
            Pageable pageable
    ) {
        return documentService.findByIdIn(request, pageable);
    }

    //делаю на скорость, можно было бы и с POST + request body, пока так решил оставить
    @Operation(summary = "Search documents",
            description = "Searches documents with optional filters by status, author, and creation date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documents found",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range or if other validation errors appears")
    })
    @GetMapping
    public Page<DocumentInfo> searchDocuments(
            @Parameter(description = "Document status filter", example = "SUBMITTED")
            @RequestParam(required = false, name = "status") DocumentStatus status,

            @Parameter(description = "Author filter (case-insensitive partial match)", example = "Бум бум")
            @RequestParam(required = false, name = "author") String author,

            @Parameter(description = "Start date for creation date range (inclusive)", example = "2024-01-01T00:00:00Z")
            @RequestParam(required = false, name = "dateFrom") OffsetDateTime dateFrom,

            @Parameter(description = "End date for creation date range (inclusive)", example = "2024-12-31T23:59:59Z")
            @RequestParam(required = false, name = "dateTo") OffsetDateTime dateTo,

            @ParameterObject
            Pageable pageable
    ) {
        DocumentSearchRequest searchRequest = new DocumentSearchRequest(status, author, dateFrom, dateTo);
        return documentService.searchDocuments(searchRequest, pageable);
    }


    @Operation(summary = "Submit documents for approval (batch)",
            description = "Attempts to transition documents from DRAFT to SUBMITTED status. Returns results for each document ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch submission completed",
                    content = @Content(schema = @Schema(implementation = SubmissionResult.class))),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/batch/submission")
    public SubmissionResult submitDocuments(@Valid @RequestBody BatchSubmissionRequest request) {
        return documentService.submitDocuments(request);
    }

    @Operation(summary = "Approve documents (batch)",
            description = "Attempts to transition documents from SUBMITTED to APPROVED status. Creates history and registry entries." +
                    " Returns results for each document ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batch approval completed",
                    content = @Content(schema = @Schema(implementation = ApprovalResult.class))),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/batch/approval")
    public ApprovalResult approveDocuments(@Valid @RequestBody BatchApprovalRequest request) {
        return documentService.approveDocuments(request);
    }

}