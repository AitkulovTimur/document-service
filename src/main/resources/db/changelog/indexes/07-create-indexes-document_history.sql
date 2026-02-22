CREATE INDEX idx_history_document_created
    ON document_history (document_id, created_at);