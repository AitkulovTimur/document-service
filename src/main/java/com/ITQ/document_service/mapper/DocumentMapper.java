package com.ITQ.document_service.mapper;


import com.ITQ.document_service.dto.response.DocumentHistoryResponse;
import com.ITQ.document_service.dto.response.DocumentInfo;
import com.ITQ.document_service.dto.response.DocumentNoHistoryResponse;
import com.ITQ.document_service.dto.response.DocumentResponse;
import com.ITQ.document_service.entity.Document;
import com.ITQ.document_service.entity.DocumentHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "status", expression = "java(document.getStatus().name())")
    DocumentInfo toDocumentInfo(Document document);

    @Mapping(target = "documentInfo", source = ".")
    @Mapping(target = "history", source = "history", qualifiedByName = "mapHistory")
    DocumentResponse toResponse(Document document);

    @Mapping(target = "documentInfo", source = ".")
    DocumentNoHistoryResponse toCreateResponse(Document document);

    @Named("mapHistory")
    default List<DocumentHistoryResponse> mapHistory(List<DocumentHistory> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        return history.stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Mapping(target = "action", expression = "java(history.getAction().name())")
    DocumentHistoryResponse toHistoryResponse(DocumentHistory history);
}