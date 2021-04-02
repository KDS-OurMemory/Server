package com.kds.ourmemory.advice.v1.memory;

import com.kds.ourmemory.advice.v1.ResultCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemoryResultCode implements ResultCode{
    NOT_FOUND("R404", "입력한 값에 해당하는 일정을 찾을 수 없습니다."),
    INTERNAL_SERVER("M500", "일정에 대한 작업 중 알 수 없는 오류가 발생하였습니다.")
    ;
    private String code;
    private String msg;
}
