CREATE TABLE documents
(
    id         BIGSERIAL PRIMARY KEY,
    number     VARCHAR(50)     NOT NULL UNIQUE,
    author     VARCHAR(255)    NOT NULL,
    title      VARCHAR(500)    NOT NULL,
    status     document_status NOT NULL,
    created_at TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at TIMESTAMP       NOT NULL DEFAULT now(),
    version    BIGINT          NOT NULL DEFAULT 0
);