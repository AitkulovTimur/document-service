package com.ITQ.document_service.service.impl;

import com.ITQ.document_service.dto.api.ErrorResponseValidation;
import com.ITQ.document_service.dto.request.ApprovalRequest;
import com.ITQ.document_service.dto.response.ConcurrentApprovalSummary;
import com.ITQ.document_service.dto.response.DocumentApprovalResult;
import com.ITQ.document_service.entity.Document;
import com.ITQ.document_service.enums.ApprovalStatus;
import com.ITQ.document_service.enums.DocumentStatus;
import com.ITQ.document_service.exception.DocumentServiceClientException;
import com.ITQ.document_service.repository.DocumentRepository;
import com.ITQ.document_service.service.ConcurrentDocumentService;
import com.ITQ.document_service.service.internal.DocumentProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link ConcurrentDocumentService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConcurrentDocumentServiceImpl implements ConcurrentDocumentService {

    private static final int MAX_SIZE_OF_ACTOR_STRING = 255;

    private final DocumentProcessor documentProcessor;
    private final DocumentRepository documentRepository;

    @Override
    public ConcurrentApprovalSummary testConcurrentApproval(Long documentId, int threads, int attempts, String actor) {

        primaryValidation(threads, attempts, actor);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<CompletableFuture<DocumentApprovalResult>> futures = new ArrayList<>(attempts);
            for (int i = 0; i < attempts; i++) {
                int attemptNo = i;
                futures.add(CompletableFuture.supplyAsync(() ->
                        documentProcessor.approveSingleDocument(
                                new ApprovalRequest(documentId, "concurrency attempt " + attemptNo),
                                actor
                        ), executor
                ));
            }

            List<DocumentApprovalResult> results = new ArrayList<>(attempts);
            int unexpectedErrorCounter = 0;

            for (CompletableFuture<DocumentApprovalResult> future : futures) {
                try {
                    results.add(future.join());
                } catch (Exception e) {
                    unexpectedErrorCounter++;
                    log.error("Unexpected error during concurrent approval attempt. documentId={}", documentId, e);
                }
            }

            return buildResult(documentId, threads, attempts, results, unexpectedErrorCounter);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Aggregates raw attempt results into a summary with counters and final document status.
     */
    private ConcurrentApprovalSummary buildResult(
            Long documentId,
            int threads,
            int attempts,
            List<DocumentApprovalResult> results,
            int unexpectedErrorCounter
    ) {
        int success = 0;
        int conflict = 0;
        int notFound = 0;
        int registryError = 0;

        for (DocumentApprovalResult r : results) {
            ApprovalStatus st = r.status();
            switch (st) {
                case SUCCESS -> success++;
                case CONFLICT -> conflict++;
                case NOT_FOUND -> notFound++;
                case REGISTRY_ERROR -> registryError++;
            }
        }

        DocumentStatus finalStatus = documentRepository.findById(documentId)
                .map(Document::getStatus)
                .orElse(null);

        return new ConcurrentApprovalSummary(
                documentId,
                threads,
                attempts,
                success,
                conflict,
                notFound,
                registryError,
                unexpectedErrorCounter,
                finalStatus
        );
    }

    /**
     * Validates basic input parameters for concurrent test execution.
     *
     * @throws DocumentServiceClientException when any of the parameters is invalid
     */
    private void primaryValidation(int threads, int attempts, String actor) {
        List<ErrorResponseValidation.FieldError> clientErrors = new ArrayList<>();

        if (threads < 1) {
            clientErrors.add(new ErrorResponseValidation
                    .FieldError("threads", String.valueOf(threads), "threads must be >= 1"));
        }
        if (attempts < 1) {
            clientErrors.add(new ErrorResponseValidation
                    .FieldError("attempts", String.valueOf(attempts), "attempts must be >= 1"));
        }
        if (StringUtils.isBlank(actor)) {
            clientErrors.add(new ErrorResponseValidation
                    .FieldError("actor", actor, "actor is empty!"));
        }
        if (actor.length() > MAX_SIZE_OF_ACTOR_STRING) {
            clientErrors.add(new ErrorResponseValidation
                    .FieldError("actor",
                    actor,
                    String.format("The length of 'actor' field can't be more than %s signs", MAX_SIZE_OF_ACTOR_STRING))
            );
        }

        if (CollectionUtils.isNotEmpty(clientErrors)) {
            throw new DocumentServiceClientException(
                    "Some of params has invalid values. Please check, fix errors and try again",
                    clientErrors);
        }
    }
}
