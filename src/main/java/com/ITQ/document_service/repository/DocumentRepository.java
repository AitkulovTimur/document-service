package com.ITQ.document_service.repository;

import com.ITQ.document_service.entity.Document;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"history"})
    Optional<Document> findById(@NonNull Long id);

    /**
     * Finds a document by its unique number.
     *
     * @param number the document number
     * @return the document with the given number
     */
    @EntityGraph(attributePaths = {"history"})
    Optional<Document> findByNumber(String number);
}
