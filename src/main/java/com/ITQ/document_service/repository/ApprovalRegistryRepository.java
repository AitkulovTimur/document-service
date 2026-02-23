package com.ITQ.document_service.repository;

import com.ITQ.document_service.entity.ApprovalRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRegistryRepository extends JpaRepository<ApprovalRegistry, Long> {

}
