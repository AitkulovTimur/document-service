package com.ITQ.document_service.mapper;

import com.ITQ.document_service.dto.DocumentResponse;
import com.ITQ.document_service.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "status", expression = "java(document.getStatus().name())")
    DocumentResponse toResponse(Document document);
}