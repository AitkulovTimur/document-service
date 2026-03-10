DROP INDEX IF EXISTS idx_documents_author;
CREATE INDEX idx_documents_author_lower ON documents (LOWER(author));
