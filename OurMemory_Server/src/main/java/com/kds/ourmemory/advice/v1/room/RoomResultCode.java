package com.kds.ourmemory.advice.v1.room;

import com.kds.ourmemory.advice.v1.ResultCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomResultCode implements ResultCode{
    /* Custom Error */
    NOT_FOUND_OWNER("R001", "방 생성자의 정보를 찾을 수 없습니다. 생성자의 회원 번호를 확인해주시기 바랍니다."),
    NOT_FOUND_MEMBER("R002", "방 참여자의 정보를 찾을 수 없습니다. 참여자의 회원 번호를 확인해주시기 바랍니다."),
    ALREADY_OWNER("R003", "해당 사용자는 이미 방장입니다. 양도할 다른 참여자를 선택해주시기 바랍니다."),

    
    /* Http Status Error */
    BAD_REQUEST("R400", "방 기능과 관련된 요청 변수 값이 잘못되었습니다. API 요청 프로토콜을 확인하시기 바랍니다."),
    NOT_FOUND("R404", "입력한 값에 해당하는 방을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("R500", "방에 대한 작업 중 알 수 없는 오류가 발생하였습니다.")
    ;
    private final String code;
    private final String msg;
}
