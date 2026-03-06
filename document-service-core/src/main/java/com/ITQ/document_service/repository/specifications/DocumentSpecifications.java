package com.ITQ.document_service.repository.specifications;

import com.ITQ.document_service.entity.Document;
import com.ITQ.document_service.enums.DocumentStatus;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for {@link Specification} instances used to query {@link Document} entities.
 */
public class DocumentSpecifications {

    /**
     * Builds a dynamic search specification for documents.
     *
     * @param status   optional document status filter (exact match)
     * @param author   optional author substring filter (case-insensitive, partial match)
     * @param dateFrom optional lower bound for {@code createdAt} field (inclusive)
     * @param dateTo   optional upper bound for {@code createdAt} field (inclusive)
     * @return JPA specification combining all provided filters with AND logic
     */
    public static Specification<Document> search(
            DocumentStatus status,
            String author,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (StringUtils.isNotBlank(author)) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("author")),
                                "%" + author.toLowerCase() + "%"
                        )
                );
            }

            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
            }

            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}