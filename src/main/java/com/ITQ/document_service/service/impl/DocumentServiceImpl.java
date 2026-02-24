package com.ITQ.document_service.service.impl;

import com.ITQ.document_service.dto.CreateDocumentRequest;
import com.ITQ.document_service.dto.DocumentResponse;
import com.ITQ.document_service.entity.Document;
import com.ITQ.document_service.enums.DocumentStatus;
import com.ITQ.document_service.mapper.DocumentMapper;
import com.ITQ.document_service.repository.DocumentRepository;
import com.ITQ.document_service.service.DocumentService;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public DocumentResponse create(CreateDocumentRequest request) {

        String nanoId = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                NanoIdUtils.DEFAULT_ALPHABET, 9);

        Document document = Document.builder()
                .author(request.author())
                .title(request.title())
                .status(DocumentStatus.DRAFT)
                .number(DOC_STR + nanoId)
                .build();

        Document saved = documentRepository.save(document);

        return documentMapper.toResponse(saved);
    }

}
