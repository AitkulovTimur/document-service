package com.ITQ.document_service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OperationForLogType {
    CREATE_DOCUMENT("[CREATE_DOCUMENT] "),
    GET_DOCUMENT("[GET_DOCUMENT] ");

    private final String operation;
}
