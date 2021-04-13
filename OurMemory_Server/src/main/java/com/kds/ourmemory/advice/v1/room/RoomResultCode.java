package com.kds.ourmemory.advice.v1.room;

import com.kds.ourmemory.advice.v1.ResultCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomResultCode implements ResultCode{
    /* Custom Error */
    DATA_RELATION_ERROR("R001", "방에 대한 관계테이블 작업 중 알 수 없는 오류가 발생하였습니다."),
    NOT_FOUND_OWNER("R002", "방 생성자의 정보를 찾을 수 없습니다. 생성자의 회원 번호를 확인해주시기 바랍니다."),

    
    /* Http Status Error */
    BAD_PARAMETER("R400", "방 기능과 관련된 요청 변수 값이 잘못되었습니다. API 요청 프로토콜을 확인하시기 바랍니다."),
    NOT_FOUND("R404", "입력한 값에 해당하는 방을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("R500", "방에 대한 작업 중 알 수 없는 오류가 발생하였습니다.")
    ;
    private String code;
    private String msg;
}
