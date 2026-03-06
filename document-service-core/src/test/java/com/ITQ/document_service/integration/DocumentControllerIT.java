package com.ITQ.document_service.integration;


import com.ITQ.document_service.dto.request.ApprovalRequest;
import com.ITQ.document_service.dto.request.BatchApprovalRequest;
import com.ITQ.document_service.dto.request.BatchDocumentRequest;
import com.ITQ.document_service.dto.request.BatchSubmissionRequest;
import com.ITQ.document_service.dto.request.CreateDocumentRequest;
import com.ITQ.document_service.dto.request.SubmissionRequest;
import com.ITQ.document_service.dto.response.DocumentInfo;
import com.ITQ.document_service.enums.DocumentStatus;
import com.ITQ.document_service.repository.ApprovalRegistryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentControllerIT extends BaseIntegrationTest {

    private static final String AUTHOR = "Ivan Ivanov";
    private static final String TITLE = "Ivan Ivanov";

    @Value("${spring.data.web.pageable.default-page-size:30}")
    private int defaultPageSize;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private ApprovalRegistryRepository approvalRegistryRepository;

    @AfterEach
    void tearDown() {
        Mockito.reset(approvalRegistryRepository);
    }

    @Test
    void shouldCreateDocumentHappyPath() throws Exception {

        // given
        var request = new CreateDocumentRequest(AUTHOR, TITLE);
        String requestAsJson = objectMapper.writeValueAsString(request);

        // when
        var result = mockMvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson));

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentInfo.author").value(AUTHOR))
                .andExpect(jsonPath("$.documentInfo.title").value(TITLE))
                .andExpect(jsonPath("$.documentInfo.status").value(DocumentStatus.DRAFT.name()))
                .andExpect(jsonPath("$.documentInfo.id").exists())
                .andExpect(jsonPath("$.documentInfo.number").exists())
                .andExpect(jsonPath("$.documentInfo.createdAt").exists())
                .andExpect(jsonPath("$.documentInfo.updatedAt").exists());
    }

    @Test
    void shouldFailValidationForEmptyFields() throws Exception {

        // given
        var request = new CreateDocumentRequest("", "");
        String requestAsJson = objectMapper.writeValueAsString(request);

        // when
        var result = mockMvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field=='author')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='title')]").exists());
    }

    @Test
    void shouldFindDocumentByIdHappyPath() throws Exception {

        // given
        var createRequest = new CreateDocumentRequest(AUTHOR, TITLE);
        String createJson = objectMapper.writeValueAsString(createRequest);

        String createResponse = mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long documentId = objectMapper.readTree(createResponse)
                .get("documentInfo")
                .get("id")
                .asLong();

        // when
        var result = mockMvc.perform(get("/api/documents/{id}", documentId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.documentInfo.id").value(documentId))
                .andExpect(jsonPath("$.documentInfo.author").value(AUTHOR))
                .andExpect(jsonPath("$.documentInfo.title").value(TITLE))
                .andExpect(jsonPath("$.documentInfo.status").value(DocumentStatus.DRAFT.name()))
                .andExpect(jsonPath("$.documentInfo.number").exists())
                .andExpect(jsonPath("$.documentInfo.createdAt").exists())
                .andExpect(jsonPath("$.documentInfo.updatedAt").exists())
                .andExpect(jsonPath("$.history").isArray());
    }

    @Test
    void shouldFailWhenDocumentNotFoundById() throws Exception {

        // given
        final Long nonExistentId = -1L;

        // when
        var result = mockMvc.perform(get("/api/documents/{id}", nonExistentId));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Document not found with id=" + nonExistentId));
    }

    @Test
    void shouldFindDocumentByNumberHappyPath() throws Exception {

        // given
        var createRequest = new CreateDocumentRequest(AUTHOR, TITLE);
        String createJson = objectMapper.writeValueAsString(createRequest);

        String createResponse = mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String documentNumber = objectMapper.readTree(createResponse)
                .get("documentInfo")
                .get("number")
                .asText();

        // when
        var result = mockMvc.perform(get("/api/documents/by-number/{number}", documentNumber));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.documentInfo.number").value(documentNumber))
                .andExpect(jsonPath("$.documentInfo.author").value(AUTHOR))
                .andExpect(jsonPath("$.documentInfo.title").value(TITLE))
                .andExpect(jsonPath("$.documentInfo.status").value(DocumentStatus.DRAFT.name()))
                .andExpect(jsonPath("$.documentInfo.id").exists())
                .andExpect(jsonPath("$.documentInfo.createdAt").exists())
                .andExpect(jsonPath("$.documentInfo.updatedAt").exists())
                .andExpect(jsonPath("$.history").isArray());
    }

    @Test
    void shouldFailWhenDocumentNotFoundByNumber() throws Exception {

        // given
        final String nonExistentNumber = "DOC—NONEXISTENT123";

        // when
        var result = mockMvc.perform(get("/api/documents/by-number/{number}", nonExistentNumber));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Document not found with number=" + nonExistentNumber));
    }

    @Test
    void shouldReturnPaginatedDocuments_whenBatchRequest() throws Exception {
        // given
        Long doc1Id = createDocument("Doc 1", "Author 1");
        Long doc2Id = createDocument("Doc 2", "Author 2");

        var request = new BatchDocumentRequest(List.of(doc1Id, doc2Id));
        String requestAsJson = objectMapper.writeValueAsString(request);

        final int expectedCountOfElements = 2;

        // when
        var result = mockMvc.perform(post("/api/documents/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(expectedCountOfElements))
                .andExpect(jsonPath("$.totalElements").value(expectedCountOfElements))
                .andExpect(jsonPath("$.size").value(defaultPageSize));
    }

    @Test
    void shouldSubmitDocumentsHappyPath() throws Exception {
        // given
        Long doc1Id = createDocument("Doc 1", AUTHOR);
        Long doc2Id = createDocument("Doc 2", AUTHOR);
        final Long nonExistentId = 1000000000L;

        var submissionRequests = Set.of(
                new SubmissionRequest(doc1Id, "Submit for review"),
                new SubmissionRequest(doc2Id, "Please approve"),
                new SubmissionRequest(nonExistentId, "Not found")
        );

        var request = new BatchSubmissionRequest(submissionRequests, "admin");
        String requestAsJson = objectMapper.writeValueAsString(request);

        // when
        var result = mockMvc.perform(post("/api/documents/batch/submission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(3))
                .andExpect(jsonPath("$.results[?(@.documentId==" + doc1Id + " && @.status=='SUCCESS')]").exists())
                .andExpect(jsonPath("$.results[?(@.documentId==" + doc2Id + " && @.status=='SUCCESS')]").exists())
                .andExpect(jsonPath("$.results[?(@.documentId==" + nonExistentId + " && @.status=='NOT_FOUND')]").exists());
    }

    @Test
    void shouldApproveDocumentsHappyPath() throws Exception {
        // given
        Long doc1Id = createDocument("Doc 1", AUTHOR);
        Long doc2Id = createDocument("Doc 2", AUTHOR);
        Long doc3Id = createDocument("Doc 3", AUTHOR);

        submitDocument(doc1Id);
        submitDocument(doc2Id);

        final Long nonExistentId = 1000000000L;

        var approvalRequests = Set.of(
                new ApprovalRequest(doc1Id, "Submit for review"),
                new ApprovalRequest(doc2Id, "Please approve"),
                new ApprovalRequest(doc3Id, "Please approve"),
                new ApprovalRequest(nonExistentId, "Not found")
        );
        var batchRequest = new BatchApprovalRequest(approvalRequests, "admin");
        String requestAsJson = objectMapper.writeValueAsString(batchRequest);

        // when
        var result = mockMvc.perform(post("/api/documents/batch/approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(4))
                .andExpect(jsonPath("$.results[?(@.documentId==" + doc1Id + " && @.status=='SUCCESS')]")
                        .exists())
                .andExpect(jsonPath("$.results[?(@.documentId==" + doc2Id + " && @.status=='SUCCESS')]")
                        .exists())
                .andExpect(jsonPath("$.results[?(@.documentId==" + doc3Id + " && @.status=='CONFLICT')]")
                        .exists())
                .andExpect(jsonPath("$.results[?(@.documentId==" + nonExistentId + " && @.status=='NOT_FOUND')]")
                        .exists());
    }

    @Test
    void shouldApproveDocumentsErrorPath() throws Exception {
        // given
        Long documentId = createDocument("Doc 1", AUTHOR);

        submitDocument(documentId);

        var approvalRequests = Set.of(
                new ApprovalRequest(documentId, "Submit for review")
        );
        var batchRequest = new BatchApprovalRequest(approvalRequests, "admin");
        String requestAsJson = objectMapper.writeValueAsString(batchRequest);

        // when
        doThrow(RuntimeException.class).when(approvalRegistryRepository).save(any());
        var result = mockMvc.perform(post("/api/documents/batch/approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(1))
                .andExpect(jsonPath("$.results[?(@.documentId==" + documentId + " && @.status=='REGISTRY_ERROR')]")
                        .exists());

        var docToCheck = mockMvc.perform(get("/api/documents/{id}", documentId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var rootNode = objectMapper.readTree(docToCheck);

        DocumentInfo document = objectMapper.treeToValue(rootNode.path("documentInfo"), DocumentInfo.class);
        assertEquals(DocumentStatus.SUBMITTED.name(), document.status());
    }

    @Test
    void shouldApproveDocumentConcurrently_andCreateSingleRegistryRecord() throws Exception {
        // given
        Long documentId = createDocument("Doc 1", AUTHOR);
        submitDocument(documentId);

        final int threads = 8;
        final int attempts = 50;

        // then
        String response = mockMvc.perform(get("/api/concurrency/documents/{id}/approve", documentId)
                        .param("threads", String.valueOf(threads))
                        .param("attempts", String.valueOf(attempts))
                        .param("actor", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(documentId))
                .andExpect(jsonPath("$.threads").value(threads))
                .andExpect(jsonPath("$.attempts").value(attempts))
                .andExpect(jsonPath("$.finalStatus").value(DocumentStatus.APPROVED.name()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var root = objectMapper.readTree(response);
        assertNotNull(root.get("success"));
        assertNotNull(root.get("registryError"));

        // verify only one registry record exists for document
        assertEquals(1, approvalRegistryRepository.findAll().stream()
                .filter(r -> r.getDocument() != null && documentId.equals(r.getDocument().getId()))
                .count());
    }

    private Long createDocument(String title, String author) throws Exception {
        var request = new CreateDocumentRequest(author, title);
        String requestAsJson = objectMapper.writeValueAsString(request);

        String createDocResponse = mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAsJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(createDocResponse)
                .path("documentInfo")
                .path("id")
                .asLong();
    }

    private void submitDocument(Long documentId) throws Exception {
        var submissionRequest = new SubmissionRequest(documentId, "Submit for approval");
        var batchRequest = new BatchSubmissionRequest(Set.of(submissionRequest), "admin");
        String requestAsJson = objectMapper.writeValueAsString(batchRequest);

        mockMvc.perform(post("/api/documents/batch/submission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestAsJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[?(@.documentId==" + documentId + " && @.status=='SUCCESS')]").exists());
    }
}