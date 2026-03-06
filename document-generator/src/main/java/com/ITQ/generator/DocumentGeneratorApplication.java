package com.ITQ.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DocumentGeneratorApplication {

    private static final Logger logger = LoggerFactory.getLogger(DocumentGeneratorApplication.class);
    private static final String CONFIG_FILE_DEFAULT = "config.properties";
    private static final String DEFAULT_API_URL = "http://localhost:8080/api/documents";
    private static final int COEFFICIENT_IO_BOUND_TASKS = 4;
    private static final int MINIMUM_THREADS = 1;
    private static final String CONNECT_TIMEOUT_SEC = "5";
    private static final String SOCKET_TIMEOUT_SEC = "10";
    private static final String RESPONSE_TIMEOUT_SEC = "20";
    private static final String DEF_TERMINATION_TIMEOUT_MIN = "10";
    private static final String DEFAULT_NUMBER_OF_DOCS = "10";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Properties properties = new Properties();

    private int totalDocs;

    public static void main(String[] args) {
        if (args.length > 0) {
            System.setProperty("config.file", args[0]);
        }

        try {
            DocumentGeneratorApplication app = new DocumentGeneratorApplication();
            app.run(null);
        } catch (Exception e) {
            logger.error("Application failed", e);
            System.exit(1);
        }
    }

    //I had to add this strange param for proper unit testing
    public void run(CloseableHttpClient customHttpClientForTest) throws IOException {
        loadConfig();

        totalDocs = Integer.parseInt(properties.getProperty("document.count", DEFAULT_NUMBER_OF_DOCS));
        if (totalDocs < MINIMUM_THREADS) {
            throw new IllegalArgumentException("Document count must be greater than or equal to " + MINIMUM_THREADS);
        }
        logger.info("Total documents to create: {}", totalDocs);

        String apiUrl = properties.getProperty("api.url", DEFAULT_API_URL);

        int threadCount = adjustMultithreadingSettings(totalDocs);

        try (CloseableHttpClient httpClient = customHttpClientForTest == null ?
                createHttpClient(threadCount) :
                customHttpClientForTest) {

            checkIfConnectionPossible(apiUrl, httpClient);
            if (totalDocs == MINIMUM_THREADS) {
                logger.info("Application finished cleanly.");
                return;
            }

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            for (int i = 2; i <= totalDocs; i++) {
                final int docNum = i;
                executor.execute(() -> {
                    try {
                        generateDocument(docNum, apiUrl, httpClient);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            executor.shutdown();
            try {
                int timeOut = Integer.parseInt(properties.getProperty(
                        "multithreading.timeout", DEF_TERMINATION_TIMEOUT_MIN));
                if (!executor.awaitTermination(timeOut, TimeUnit.MINUTES)) {
                    logger.warn("Not all tasks finished, forcing shutdown...");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        logger.info("Application finished cleanly.");
    }

    //try first call to clarify situation with the connection with the core service
    private void checkIfConnectionPossible(String apiUrl, CloseableHttpClient httpClient) {
        final int firstDocIndex = 1;
        try {
            generateDocument(firstDocIndex, apiUrl, httpClient);
        } catch (IOException e) {
            final String commonPart = "Connection refused";

            if (e.getMessage().contains(commonPart)) {
                logger.error("{}. Please check if the core service is running", commonPart, e);
                System.exit(1);
            }
        }
    }


    private void loadConfig() throws IOException {
        String configFile = System.getProperty("config.file", CONFIG_FILE_DEFAULT);
        try (var configStreamForLoad = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (configStreamForLoad == null) {
                throw new IOException("config.properties not found in resources");
            }
            properties.load(configStreamForLoad);
        }
    }

    //dynamic calculation of threads. Depends on doc number
    private int adjustMultithreadingSettings(int totalDocuments) {
        int cores = Runtime.getRuntime().availableProcessors();

        int maxThreadsAllowed = cores * COEFFICIENT_IO_BOUND_TASKS;
        int threadCount = Math.max(MINIMUM_THREADS, Math.min(totalDocuments, maxThreadsAllowed));

        logger.info("Detected {} cores. Using {} thread(s) for {} documents", cores, threadCount, totalDocuments);

        return threadCount;
    }

    private CloseableHttpClient createHttpClient(int threadCount) {
        int connectTimeoutSec = Integer.parseInt(properties
                .getProperty("http.connectTimeoutSec", CONNECT_TIMEOUT_SEC));
        int socketTimeoutSec = Integer.parseInt(properties
                .getProperty("http.socketTimeoutSec", SOCKET_TIMEOUT_SEC));
        int responseTimeoutSec = Integer.parseInt(properties
                .getProperty("http.responseTimeoutSec", RESPONSE_TIMEOUT_SEC));

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(threadCount);
        cm.setDefaultMaxPerRoute(threadCount);

        cm.setDefaultConnectionConfig(
                ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofSeconds(connectTimeoutSec))
                        .build()
        );

        cm.setDefaultSocketConfig(
                SocketConfig.custom()
                        .setSoTimeout(Timeout.ofSeconds(socketTimeoutSec))
                        .build()
        );

        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofSeconds(responseTimeoutSec))
                .build();

        return HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    private void generateDocument(int documentNumber, String apiUrl, CloseableHttpClient httpClient)
            throws IOException {
        Map<String, Object> document = Map.of(
                "title", "Generated Document " + documentNumber,
                "author", "Document generator service"
        );

        String jsonDocument = objectMapper.writeValueAsString(document);

        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setEntity(new StringEntity(jsonDocument, ContentType.APPLICATION_JSON));

        logger.info("Sending request for document {}", documentNumber);

        httpClient.execute(httpPost, response -> {
            int statusCode = response.getCode();
            if (statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_REDIRECTION) {
                logger.info("Successfully generated document {}/{} (status: {})",
                        documentNumber, totalDocs, statusCode);
            } else {
                logger.error("Failed to generate document {} (status: {})",
                        documentNumber, statusCode);
            }
            return null;
        });
    }
}
