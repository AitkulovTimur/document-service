CREATE TABLE document_history
(
    id          BIGSERIAL PRIMARY KEY,
    document_id BIGINT          NOT NULL,
    action      document_action NOT NULL,
    actor       VARCHAR(255)    NOT NULL,
    comment     TEXT,
    created_at  TIMESTAMP       NOT NULL DEFAULT now(),

    CONSTRAINT fk_history_document
        FOREIGN KEY (document_id)
            REFERENCES documents (id)
            ON DELETE CASCADE
);