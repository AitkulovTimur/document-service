CREATE TABLE approval_registry
(
    id          BIGSERIAL PRIMARY KEY,
    document_id BIGINT       NOT NULL UNIQUE,
    approved_by VARCHAR(255) NOT NULL,
    approved_at TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT fk_registry_document
        FOREIGN KEY (document_id)
            REFERENCES documents (id)
            ON DELETE CASCADE
);