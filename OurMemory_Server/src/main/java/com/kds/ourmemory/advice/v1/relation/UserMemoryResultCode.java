package com.kds.ourmemory.advice.v1.relation;

import com.kds.ourmemory.advice.v1.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserMemoryResultCode implements ResultCode {

    /* Http Status Error */
    INTERNAL_SERVER_ERROR("UM500","사용자-일정 관계테이블 작업 중 알 수 없는 오류가 발생하였습니다.")
    ;

    private final String code;
    private final String msg;
}
