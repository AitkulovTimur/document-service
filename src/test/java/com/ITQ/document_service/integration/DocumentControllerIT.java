package com.ITQ.document_service.integration;

import com.ITQ.document_service.dto.CreateDocumentRequest;
import com.ITQ.document_service.enums.DocumentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentControllerIT extends BaseIntegrationTest {

    private static final String AUTHOR = "Ivan Ivanov";
    private static final String TITLE = "Ivan Ivanov";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        Long nonExistentId = -1L;

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
        String nonExistentNumber = "DOC—NONEXISTENT123";

        // when
        var result = mockMvc.perform(get("/api/documents/by-number/{number}", nonExistentNumber));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Document not found with number=" + nonExistentNumber));
    }
}