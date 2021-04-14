package com.kds.ourmemory.advice.v1.memory;

import com.kds.ourmemory.advice.v1.ResultCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemoryResultCode implements ResultCode{
    /* Custom Error */
    NOT_FOUND_WRITER("M001", "일정 생성자의 정보를 찾을 수 없습니다. 생성자의 회원 번호를 확인해주시기 바랍니다."),
    NOT_FOUND_ROOM("M002", "일정을 등록시킬 방을 찾을 수 없습니다. 방 번호를 확인해주시기 바랍니다."),
    
    
    /* Http Status Error */
    BAD_PARAMETER("M400", "일정 기능과 관련된 요청 변수 값이 잘못되었습니다. API 요청 프로토콜을 확인하시기 바랍니다."),
    NOT_FOUND("M404", "입력한 값에 해당하는 일정을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("M500", "일정에 대한 작업 중 알 수 없는 오류가 발생하였습니다.")
    ;
    private String code;
    private String msg;
}
