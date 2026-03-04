package com.ITQ.document_service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OperationForLogType {
    CREATE_DOCUMENT("[CREATE_DOCUMENT] "),
    SUBMIT_DOCUMENT("[SUBMIT_DOCUMENT] "),
    APPROVE_DOCUMENT("[APPROVE_DOCUMENT] "),
    SEARCH_DOCUMENTS("[SEARCH_DOCUMENTS] "),
    GET_DOCUMENT("[GET_DOCUMENT] ");

    private final String operation;
}
