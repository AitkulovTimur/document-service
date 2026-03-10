package com.ITQ.document_service.repository;

import com.ITQ.document_service.entity.Document;
import com.ITQ.document_service.enums.DocumentStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

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

    /**
     * Finds documents by their IDs with pagination and sorting.
     *
     * @param ids      list of document IDs
     * @param pageable pagination and sorting parameters
     * @return page of documents with their history
     */
    @EntityGraph(attributePaths = {"history"})
    @Query("SELECT d FROM Document d WHERE d.id IN :ids")
    Page<Document> findByIdIn(List<Long> ids, Pageable pageable);

    /**
     * Finds document IDs by status with a limit for batch processing.
     *
     * @param status   the document status
     * @param pageable pageable param for limit
     * @return list of document IDs with the given status
     */
    @Query("SELECT d.id FROM Document d WHERE d.status = :status ORDER BY d.id")
    List<Long> findIdsByStatusWithLimit(
            @Param("status") DocumentStatus status,
            Pageable pageable
    );
}
