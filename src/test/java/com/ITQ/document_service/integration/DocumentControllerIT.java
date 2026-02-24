package com.ITQ.document_service.integration;

import com.ITQ.document_service.dto.CreateDocumentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentControllerIT extends BaseIntegrationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateDocumentHappyPath() throws Exception {
        String author = "Ivan Ivanov";
        String title = "Supply contract";

        var request = new CreateDocumentRequest(
                author,
                title
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author").value(author))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.number").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldFailValidationForEmptyFields() throws Exception {

        var request = new com.ITQ.document_service.dto.CreateDocumentRequest(
                "",
                ""
        );

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field=='author')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='title')]").exists());
    }

}