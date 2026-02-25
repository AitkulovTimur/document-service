package com.ITQ.document_service.service.impl;

import com.ITQ.document_service.dto.BatchDocumentRequest;
import com.ITQ.document_service.dto.CreateDocumentRequest;
import com.ITQ.document_service.dto.DocumentNoHistoryResponse;
import com.ITQ.document_service.dto.DocumentResponse;
import com.ITQ.document_service.entity.Document;
import com.ITQ.document_service.enums.DocumentStatus;
import com.ITQ.document_service.enums.OperationForLogType;
import com.ITQ.document_service.exception.DocumentNotFoundException;
import com.ITQ.document_service.mapper.DocumentMapper;
import com.ITQ.document_service.repository.DocumentRepository;
import com.ITQ.document_service.service.DocumentService;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private static final String DOC_STR = "DOC—";
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional
    public DocumentNoHistoryResponse create(CreateDocumentRequest request) {
        final String author = request.author();
        final String title = request.title();

        log.info("{}Create document with author '{}' and title '{}'",
                OperationForLogType.CREATE_DOCUMENT.getOperation(),
                author, title);

        String nanoId = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                NanoIdUtils.DEFAULT_ALPHABET, 9);

        Document document = Document.builder()
                .author(author)
                .title(title)
                .status(DocumentStatus.DRAFT)
                .number(DOC_STR + nanoId)
                .build();

        Document saved = documentRepository.save(document);

        log.info("{}Document with id '{}' and number '{}' has been created",
                OperationForLogType.CREATE_DOCUMENT.getOperation(), saved.getId(), saved.getNumber());
        return documentMapper.toCreateResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse findById(Long id) {
        log.info("{}Retrieving document with id '{}'", OperationForLogType.GET_DOCUMENT, id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));

        return documentMapper.toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse findByNumber(String number) {
        log.info("{}Retrieving document with number '{}'", OperationForLogType.GET_DOCUMENT, number);

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
}
