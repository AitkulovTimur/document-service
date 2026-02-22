CREATE INDEX idx_documents_status
    ON documents (status);

CREATE INDEX idx_documents_author
    ON documents (author);

CREATE INDEX idx_documents_created_at
    ON documents (created_at);
