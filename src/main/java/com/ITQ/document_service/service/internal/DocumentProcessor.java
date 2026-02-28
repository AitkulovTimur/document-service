package com.ITQ.document_service.service.internal;

import com.ITQ.document_service.dto.DocumentSubmissionResult;
import com.ITQ.document_service.dto.SubmissionRequest;
import com.ITQ.document_service.entity.Document;
import com.ITQ.document_service.entity.DocumentHistory;
import com.ITQ.document_service.enums.DocumentAction;
import com.ITQ.document_service.enums.DocumentStatus;
import com.ITQ.document_service.enums.OperationForLogType;
import com.ITQ.document_service.enums.SubmissionStatus;
import com.ITQ.document_service.repository.DocumentHistoryRepository;
import com.ITQ.document_service.repository.DocumentRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessor {

    private final DocumentRepository documentRepository;
    private final DocumentHistoryRepository documentHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DocumentSubmissionResult submitSingleDocument(@NonNull SubmissionRequest request, @NonNull String actor) {
        Long documentId = request.id();

        log.debug("{}Processing document submission for ID: {}", OperationForLogType.SUBMIT_DOCUMENT.getOperation(), documentId);

        return documentRepository.findById(documentId)
                .map(doc -> processDraft(doc, request, actor))
                .orElseGet(() -> {
                    log.debug("{}Document with ID {} not found",
                            OperationForLogType.SUBMIT_DOCUMENT.getOperation(), documentId);
                    return new DocumentSubmissionResult(documentId, SubmissionStatus.NOT_FOUND);
                });
    }

    private DocumentSubmissionResult processDraft(Document document, SubmissionRequest request, String actor) {
        if (document.getStatus() != DocumentStatus.DRAFT) {
            log.debug("{}Document with ID {} has status {}, cannot submit from DRAFT",
                    OperationForLogType.SUBMIT_DOCUMENT.getOperation(), document.getId(), document.getStatus());
            return new DocumentSubmissionResult(document.getId(), SubmissionStatus.CONFLICT);
        }
        document.setStatus(DocumentStatus.SUBMITTED);

        log.debug("{}History entity for document with ID {} will be created. Actor: {}",
                OperationForLogType.SUBMIT_DOCUMENT.getOperation(), document.getId(), actor);
        DocumentHistory history = DocumentHistory.builder()
                .document(document)
                .actor(actor)
                .comment(request.comment())
                .action(DocumentAction.SUBMIT)
                .build();

        documentHistoryRepository.save(history);

        log.debug("{}Document with ID {} successfully submitted for approval (DRAFT -> SUBMITTED)",
                OperationForLogType.SUBMIT_DOCUMENT.getOperation(), document.getId());
        return new DocumentSubmissionResult(document.getId(), SubmissionStatus.SUCCESS);
    }

}
