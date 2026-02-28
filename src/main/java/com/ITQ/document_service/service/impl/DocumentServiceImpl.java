package com.ITQ.document_service.service.impl;

import com.ITQ.document_service.dto.BatchDocumentRequest;
import com.ITQ.document_service.dto.BatchSubmissionRequest;
import com.ITQ.document_service.dto.CreateDocumentRequest;
import com.ITQ.document_service.dto.DocumentNoHistoryResponse;
import com.ITQ.document_service.dto.DocumentResponse;
import com.ITQ.document_service.dto.DocumentSubmissionResult;
import com.ITQ.document_service.dto.SubmissionRequest;
import com.ITQ.document_service.dto.SubmissionResult;
import com.ITQ.document_service.entity.Document;
import com.ITQ.document_service.enums.DocumentStatus;
import com.ITQ.document_service.enums.OperationForLogType;
import com.ITQ.document_service.exception.DocumentNotFoundException;
import com.ITQ.document_service.mapper.DocumentMapper;
import com.ITQ.document_service.repository.DocumentRepository;
import com.ITQ.document_service.service.DocumentService;
import com.ITQ.document_service.service.internal.DocumentProcessor;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private static final String DOC_STR = "DOC—";
    //approximate number of objects that leans to slow down the batch operation
    private static final int PARALLEL_THRESHOLD = 50;

    private final DocumentRepository documentRepository;
    private final DocumentProcessor documentProcessor;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional
    public DocumentNoHistoryResponse create(CreateDocumentRequest request) {
        final String author = request.author();
        final String title = request.title();

        log.info("{}Create document with author '{}' and title '{}'",
                OperationForLogType.CREATE_DOCUMENT.getOperation(),
                author, title);

        Document saved = new Document();
        for (int i = 0; i < 1000; i++) {
            String nanoId = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                    NanoIdUtils.DEFAULT_ALPHABET, 9);

            Document document = Document.builder()
                    .author(author)
                    .title(title)
                    .status(DocumentStatus.DRAFT)
                    .number(DOC_STR + nanoId)
                    .build();

            saved = documentRepository.save(document);
        }


        log.info("{}Document with id '{}' and number '{}' has been created",
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
        log.info("{}Retrieving documents by IDs with pagination and sorting", OperationForLogType.GET_DOCUMENT);

        Page<Document> documentPage = documentRepository.findByIdIn(request.ids(), pageable);

        return documentPage.map(documentMapper::toResponse);
    }

    @Override
    public SubmissionResult submitDocuments(BatchSubmissionRequest request) {

        Map<Long, SubmissionRequest> uniqueRequests = request.idsWithComments()
                .stream()
                .collect(Collectors.toMap(
                        SubmissionRequest::id,
                        r -> r,
                        (existing, duplicate) -> existing
                ));

        int size = uniqueRequests.size();

        log.info("{}Submitting {} documents for approval: {}",
                OperationForLogType.SUBMIT_DOCUMENT.getOperation(),
                size,
                uniqueRequests.keySet()
        );

        Stream<SubmissionRequest> stream =
                size > PARALLEL_THRESHOLD
                        ? uniqueRequests.values().parallelStream()
                        : uniqueRequests.values().stream();

        List<DocumentSubmissionResult> results = stream
                .map(req -> documentProcessor.submitSingleDocument(req, request.actor()))
                .toList();

        log.info("{}Batch submission completed. Processed {} documents",
                OperationForLogType.SUBMIT_DOCUMENT.getOperation(),
                results.size()
        );

        return new SubmissionResult(results);
    }
}
