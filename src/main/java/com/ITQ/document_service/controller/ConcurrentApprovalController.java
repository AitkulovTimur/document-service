package com.ITQ.document_service.controller;

import com.ITQ.document_service.dto.response.ConcurrentApprovalSummary;
import com.ITQ.document_service.service.ConcurrentDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes endpoints used to test concurrent approval of a single document.
 *
 * <p>This controller is intended for concurrency testing/verification rather than core business flows.</p>
 */
@RestController
@RequestMapping("/api/concurrency")
@RequiredArgsConstructor
@Tag(name = "Concurrency", description = "Concurrency testing API")
public class ConcurrentApprovalController {

    private final ConcurrentDocumentService concurrentDocumentService;

    /**
     * Runs a concurrent approval test for a single document.
     *
     * @param documentId document id to approve
     * @param threads    size of the thread pool used to execute attempts
     * @param attempts   total number of approval attempts
     * @param actor      actor name used for all attempts
     * @return summary containing counts by result status and the final document status
     */
    @Operation(
            summary = "Concurrent document approval test",
            description = "Runs multiple parallel attempts to approve the same document and returns a summary"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Test completed"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @GetMapping("/documents/{id}/approve")
    @ResponseStatus(HttpStatus.OK)
    public ConcurrentApprovalSummary testConcurrentApprove(
            @Parameter(description = "Document id", example = "1")
            @PathVariable("id") Long documentId,
            @Parameter(description = "Number of threads", example = "10")
            @RequestParam int threads,
            @Parameter(description = "Number of attempts", example = "100")
            @RequestParam int attempts,
            @Parameter(description = "Actor", example = "admin")
            @RequestParam(defaultValue = "concurrency-test") String actor
    ) {
        return concurrentDocumentService.testConcurrentApproval(documentId, threads, attempts, actor);
    }
}
