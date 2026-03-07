package com.ITQ.document_service.service.impl;

import com.ITQ.document_service.config.SchedulerConfig.WorkerProperties;
import com.ITQ.document_service.dto.request.ApprovalRequest;
import com.ITQ.document_service.dto.request.BatchApprovalRequest;
import com.ITQ.document_service.dto.request.BatchSubmissionRequest;
import com.ITQ.document_service.dto.request.SubmissionRequest;
import com.ITQ.document_service.enums.DocumentStatus;
import com.ITQ.document_service.repository.DocumentRepository;
import com.ITQ.document_service.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentBackgroundWorkers {

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final WorkerProperties workerProperties;

    @Scheduled(fixedDelayString = "#{@workerProperties.submitWorkerDelay}",
            initialDelayString = "#{@workerProperties.workerStartDelay}")
    @Transactional(readOnly = true)
    public void submitWorker() {
        try {
            log.info("SUBMIT-worker started processing");

            List<Long> documentIds = documentRepository.findIdsByStatusWithLimit(
                    DocumentStatus.DRAFT,
                    PageRequest.of(0, workerProperties.getBatchSize())
            );

            if (documentIds.isEmpty()) {
                log.info("SUBMIT-worker: No DRAFT documents found");
                return;
            }

            log.info("SUBMIT-worker found {} DRAFT documents to process", documentIds.size());

            Set<SubmissionRequest> submissionRequests = documentIds.stream()
                    .map(id -> new SubmissionRequest(id,
                            "created in scheduled SUBMIT-worker"))
                    .collect(Collectors.toSet());

            BatchSubmissionRequest request = new BatchSubmissionRequest(
                    submissionRequests,
                    "system-worker"
            );

            documentService.submitDocuments(request);
            log.info("SUBMIT-worker successfully processed {} documents", documentIds.size());

        } catch (Exception e) {
            log.error("SUBMIT-worker encountered error during processing", e);
        }
    }

    @Scheduled(fixedDelayString = "#{@workerProperties.approveWorkerDelay}",
            initialDelayString = "#{@workerProperties.workerStartDelay}")
    @Transactional(readOnly = true)
    public void approveWorker() {
        try {
            log.info("APPROVE-worker started processing");

            List<Long> documentIds = documentRepository.findIdsByStatusWithLimit(
                    DocumentStatus.SUBMITTED,
                    PageRequest.of(0, workerProperties.getBatchSize())
            );

            if (documentIds.isEmpty()) {
                log.info("APPROVE-worker: No SUBMITTED documents found");
                return;
            }

            log.info("APPROVE-worker found {} SUBMITTED documents to process", documentIds.size());
            Set<ApprovalRequest> approvalRequests = documentIds.stream()
                    .map(id -> new ApprovalRequest(id,
                            "created in scheduled APPROVE-worker"))
                    .collect(Collectors.toSet());


            BatchApprovalRequest request = new BatchApprovalRequest(
                    new HashSet<>(approvalRequests),
                    "system-worker"
            );

            documentService.approveDocuments(request);
            log.info("APPROVE-worker successfully processed {} documents", documentIds.size());

        } catch (Exception e) {
            log.error("APPROVE-worker encountered error during processing", e);
        }
    }
}
