package com.kds.ourmemory.advice.v1.friend;

import com.kds.ourmemory.advice.v1.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FriendResultCode implements ResultCode {
    /* Custom Error */
    NOT_FOUND_USER("F001", "사용자의 회원 정보를 찾을 수 없습니다. 사용자의 회원 번호를 확인해주시기 바랍니다."),
    NOT_FOUND_FRIEND("F002", "친구의 회원 정보를 찾을 수 없습니다. 친구의 회원 번호를 확인해주시기 바랍니다."),
    ALREADY_ACCEPT("F003", "이미 친구 요청을 수락한 사람입니다. 친구 추가를 진행해주시기 바랍니다."),
    BLOCKED_FROM_FRIEND("F004", "상대방이 차단했기 때문에 친구 추가를 진행할 수 없습니다."),
    NOT_REQUESTED("F005", "친구 요청없이 친구 추가할 수 없습니다. 친구 요청 먼저 진행해주시기 바랍니다."),
    STATUS_ERROR("F006", "친구 상태값이 잘못되었습니다. 관리자에게 문의하시기 바랍니다."),

    /* Http Status Error */
    BAD_REQUEST("F400", "친구 목록 기능과 관련된 요청 변수 값이 잘못되었습니다. API 요청 프로토콜을 확인하시기 바랍니다."),
    NOT_FOUND("F404", "입력한 값에 해당하는 친구를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("F500", "친구 목록에 대한 작업 중 알 수 없는 오류가 발생하였습니다.")
    ;

    private final String code;
    private final String msg;
}
