package com.ITQ.document_service.controller;

import com.ITQ.document_service.dto.BatchDocumentRequest;
import com.ITQ.document_service.dto.CreateDocumentRequest;
import com.ITQ.document_service.dto.DocumentNoHistoryResponse;
import com.ITQ.document_service.dto.DocumentResponse;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
            @Parameter(description = "Unique identifier of the document", example = "1")
            @PathVariable Long id
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
            @Parameter(description = "Business number of the document", example = "DOC-2024-001")
            @PathVariable String number
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

}