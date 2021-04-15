package com.kds.ourmemory.advice.v1.user;

import com.kds.ourmemory.advice.v1.ResultCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserResultCode implements ResultCode{
    /* Http Status Error */
    BAD_REQUEST("U400", "사용자 기능과 관련된 요청 변수 값이 잘못되었습니다. API 요청 프로토콜을 확인하시기 바랍니다."),
    NOT_FOUND("U404", "입력한 값에 해당하는 회원을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("U500", "회원에 대한 작업 중 알 수 없는 오류가 발생하였습니다.")
    ;
    
    private String code;
    private String msg;
}
