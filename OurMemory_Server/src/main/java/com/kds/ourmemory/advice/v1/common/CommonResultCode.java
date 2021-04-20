package com.kds.ourmemory.advice.v1.common;

import com.kds.ourmemory.advice.v1.ResultCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonResultCode implements ResultCode{
    SUCCESS("00", "성공"),

    /* Custom Error */
    INCORRECT_RESULT_SIZE("C001", "DB로부터 조회된 데이터 갯수가 잘못되었습니다."),
    
    
    /* Http Status Error */
    BAD_REQUEST("C400", "요청 변수 값이 잘못되었습니다. API 요청 프로토콜을 확인하시기 바랍니다."),
    NOT_FOUND("C404", "URL이 잘못되었습니다."),
    UNSUPPORTED_MEDIA_TYPE("C415", "지원하지 않는 미디어타입입니다. contentType 을 applicaion/json 으로 설정하고 다시 시도해주십시오."),
    INTERNAL_SERVER_ERROR("C500", "알 수 없는 오류가 발생하였습니다.")
    ;
    
    private String code;
    private String msg;
}
