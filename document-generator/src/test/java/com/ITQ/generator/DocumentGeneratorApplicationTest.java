package com.ITQ.generator;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class DocumentGeneratorApplicationTest {

    @Mock
    private CloseableHttpClient mockHttpClient;

    @Test
    void testMainMethod() {
        System.setProperty("config.file", "test-config.properties");
        assertDoesNotThrow(() -> {
            DocumentGeneratorApplication documentGeneratorApplication = new DocumentGeneratorApplication();
            documentGeneratorApplication.run(mockHttpClient);
        });
    }
}
