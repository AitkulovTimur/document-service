package com.ITQ.document_service.repository;

import com.ITQ.document_service.entity.DocumentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentHistoryRepository extends JpaRepository<DocumentHistory, Long> {

}
