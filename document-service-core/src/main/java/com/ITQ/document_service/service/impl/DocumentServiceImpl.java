package com.ITQ.document_service.service.impl;


import com.ITQ.document_service.dto.request.BatchApprovalRequest;
import com.ITQ.document_service.dto.request.BatchDocumentRequest;
import com.ITQ.document_service.dto.request.BatchRequest;
import com.ITQ.document_service.dto.request.BatchSubmissionRequest;
import com.ITQ.document_service.dto.request.CreateDocumentRequest;
import com.ITQ.document_service.dto.request.DocumentSearchRequest;
import com.ITQ.document_service.dto.request.HasId;
import com.ITQ.document_service.dto.response.ApprovalResult;
import com.ITQ.document_service.dto.response.DocumentApprovalResult;
import com.ITQ.document_service.dto.response.DocumentInfo;
import com.ITQ.document_service.dto.response.DocumentNoHistoryResponse;
import com.ITQ.document_service.dto.response.DocumentResponse;
import com.ITQ.document_service.dto.response.DocumentSubmissionResult;
import com.ITQ.document_service.dto.response.SubmissionResult;
import com.ITQ.document_service.entity.Document;
import com.ITQ.document_service.enums.DocumentAction;
import com.ITQ.document_service.enums.DocumentStatus;
import com.ITQ.document_service.enums.OperationForLogType;
import com.ITQ.document_service.exception.DocumentNotFoundException;
import com.ITQ.document_service.exception.SearchDocumentClientException;
import com.ITQ.document_service.mapper.DocumentMapper;
import com.ITQ.document_service.repository.DocumentRepository;
import com.ITQ.document_service.repository.specifications.DocumentSpecifications;
import com.ITQ.document_service.service.DocumentService;
import com.ITQ.document_service.service.internal.DocumentProcessor;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {
    private static final String DOC_STR = "DOC—";
    private final DocumentRepository documentRepository;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final DocumentProcessor documentProcessor;
    private final Executor documentExecutor;
    private final DocumentMapper documentMapper;

    @Value("${app-params.parallel-threshold:50}")
    private int parallelThreshold;
    @Value("${app-params.nano-id-size:9}")
    private int nanoIdSize;
    @Value("${app-params.logger-period:10}")
    private int loggerPeriod;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            ThreadPoolTaskScheduler taskScheduler,
            DocumentProcessor documentProcessor,
            @Qualifier("documentSubmitOrApproveExecutor") Executor documentExecutor,
            DocumentMapper documentMapper
    ) {
        this.documentRepository = documentRepository;
        this.taskScheduler = taskScheduler;
        this.documentProcessor = documentProcessor;
        this.documentExecutor = documentExecutor;
        this.documentMapper = documentMapper;
    }

    @Override
    @Transactional
    public DocumentNoHistoryResponse create(CreateDocumentRequest request) {
        final String author = request.author();
        final String title = request.title();

        log.info("{}Creating document with author '{}' and title '{}'...",
                OperationForLogType.CREATE_DOCUMENT.getOperation(),
                author, title);

        String nanoId = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                NanoIdUtils.DEFAULT_ALPHABET, nanoIdSize);

        Document document = Document.builder()
                .author(author)
                .title(title)
                .status(DocumentStatus.DRAFT)
                .number(DOC_STR + nanoId)
                .build();

        Document saved = documentRepository.save(document);

        log.info("{}Document with id '{}' and number '{}' has been created!",
                OperationForLogType.CREATE_DOCUMENT.getOperation(), saved.getId(), saved.getNumber());
        return documentMapper.toCreateResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse findById(Long id) {
        log.info("{}Retrieving document with id '{}'", OperationForLogType.GET_DOCUMENT.getOperation(), id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        return documentMapper.toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse findByNumber(String number) {
        log.info("{}Retrieving document with number '{}'", OperationForLogType.GET_DOCUMENT.getOperation(), number);

        Document document = documentRepository.findByNumber(number)
                .orElseThrow(() -> new DocumentNotFoundException(number));

        return documentMapper.toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> findByIdIn(BatchDocumentRequest request, Pageable pageable) {
        log.info("{}Retrieving documents by IDs with pagination and sorting",
                OperationForLogType.GET_DOCUMENT.getOperation());

        Page<Document> documentPage = documentRepository.findByIdIn(request.ids(), pageable);

        return documentPage.map(documentMapper::toResponse);
    }

    @Override
    public SubmissionResult submitDocuments(BatchSubmissionRequest request) {
        List<DocumentSubmissionResult> results = executeBatch(
                documentProcessor::submitSingleDocument,
                request,
                OperationForLogType.SUBMIT_DOCUMENT.getOperation(),
                DocumentAction.SUBMIT.name().toLowerCase()
        );
        return new SubmissionResult(results);
    }

    @Override
    public ApprovalResult approveDocuments(BatchApprovalRequest request) {
        List<DocumentApprovalResult> results = executeBatch(
                documentProcessor::approveSingleDocument,
                request,
                OperationForLogType.APPROVE_DOCUMENT.getOperation(),
                DocumentAction.APPROVE.name().toLowerCase()
        );
        return new ApprovalResult(results);
    }

    private <REQ extends HasId, RES> List<RES> executeBatch(
            BiFunction<REQ, String, RES> processor,
            BatchRequest<REQ> batchRequest,
            String logType,
            String actionName
    ) {
        //if there will be any repeated ids.
        Map<Long, REQ> uniqueRequests = batchRequest.idsWithComments().stream()
                .collect(Collectors.toMap(
                        HasId::id,
                        r -> r,
                        (existing, duplicate) -> existing
                ));

        int totalDocsToProcess = uniqueRequests.size();
        log.info("{}{} {} documents with ids: {}",
                logType, actionName, totalDocsToProcess, uniqueRequests.keySet());

        LongAdder processedCount = new LongAdder();
        //parallel processing
        LongAdder failedCountParallel = new LongAdder();
        //single thread processing
        int failedCounterSingle = 0;

        ScheduledFuture<?> progressLogger = null;

        boolean isParallelProcessing = totalDocsToProcess > parallelThreshold;
        if (isParallelProcessing) {
            progressLogger = taskScheduler.scheduleAtFixedRate(() -> {
                long currentProcessed = processedCount.sum();
                if (currentProcessed > 0) {
                    logProgress(logType, actionName, totalDocsToProcess, currentProcessed, failedCountParallel.sum());
                }
            }, Duration.ofMillis(loggerPeriod));
        }

        try {
            if (isParallelProcessing) {
                List<CompletableFuture<RES>> futures = uniqueRequests.values()
                        .stream()
                        .map(req ->
                                CompletableFuture
                                        .supplyAsync(
                                                () -> processor.apply(req, batchRequest.actor()),
                                                documentExecutor
                                        )
                                        .handle((result, ex) -> {
                                            if (ex == null) {
                                                processedCount.increment();
                                                return result;
                                            } else {
                                                failedCountParallel.increment();
                                                processedCount.increment();
                                                logUnexpectedException(logType, req.id(), ex);
                                                return null;
                                            }

                                        })
                        )
                        .toList();

                return futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .toList();
            } else {
                int progressCounter = 0;
                List<RES> resultList = new ArrayList<>();
                for (REQ req : uniqueRequests.values()) {
                    try {
                        RES operationResult = processor.apply(req, batchRequest.actor());
                        progressCounter++;
                        resultList.add(operationResult);
                    } catch (Exception e) {
                        logUnexpectedException(logType, req.id(), e);
                        failedCounterSingle++;
                    }

                    logProgress(logType, actionName, totalDocsToProcess, progressCounter, failedCounterSingle);
                }
                return resultList;
            }
        } finally {
            if (progressLogger != null) {
                progressLogger.cancel(false);
            }
            log.info("{}Batch {} completed. Processed {} documents, failed {}",
                    logType, actionName, totalDocsToProcess, isParallelProcessing ?
                            failedCountParallel.sum() :
                            failedCounterSingle
            );
        }
    }

    private void logProgress(String logType, String actionName, int total, long processed, long failed) {
        double progressPercent = (total > 0) ? (processed * 100.0 / total) : 0;

        log.info("{}Progress [{}]: {}/{} ({}%) | Failed: {}",
                logType,
                actionName.toUpperCase(),
                processed,
                total,
                String.format("%.1f", progressPercent),
                failed);
    }

    private void logUnexpectedException(String logType, Long requestId, Throwable exception) {
        log.error(
                "{}Error processing document ID {}: {}",
                logType,
                requestId,
                exception.getMessage()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentInfo> searchDocuments(DocumentSearchRequest request, Pageable pageable) {
        final DocumentStatus status = request.status();
        final String author = request.author();
        final OffsetDateTime dateFrom = request.dateFrom();
        final OffsetDateTime dateTo = request.dateTo();

        log.info("{}Searching documents with filters: status={}, author={}, dateFrom={}, dateTo={}",
                OperationForLogType.SEARCH_DOCUMENTS.getOperation(),
                status, author,
                dateFrom, dateTo);

        if (!request.isValidDateRange()) {
            throw new SearchDocumentClientException("Wrong parameter value. Date from cannot be after date to");
        }

        Page<Document> documentPage = documentRepository.findAll(
                DocumentSpecifications.search(status, author, dateFrom, dateTo),
                pageable
        );

        log.debug("{}Found {} documents matching search criteria", OperationForLogType.SEARCH_DOCUMENTS.getOperation(),
                documentPage.getTotalElements());

        return documentPage.map(documentMapper::toDocumentInfo);
    }
}
