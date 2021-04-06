package com.kds.ourmemory.advice.v1.memory;

import com.kds.ourmemory.advice.v1.ResultCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemoryResultCode implements ResultCode{
    DATA_RELATION_ERROR("M001", "일정에 대한 관계테이블 작업 중 알 수 없는 오류가 발생하였습니다."),
    NOT_FOUND_WRITER("M002", "일정 생성자의 정보를 찾을 수 없습니다. 생성자의 회원 번호를 확인해주시기 바랍니다."),
    
    NOT_FOUND("M404", "입력한 값에 해당하는 일정을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("M500", "일정에 대한 작업 중 알 수 없는 오류가 발생하였습니다.")
    ;
    private String code;
    private String msg;
}
