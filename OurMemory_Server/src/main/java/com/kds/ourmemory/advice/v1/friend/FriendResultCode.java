package com.kds.ourmemory.advice.v1.friend;

import com.kds.ourmemory.advice.v1.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FriendResultCode implements ResultCode {
    /* Http Status Error */
    BAD_REQUEST("F400", "친구 목록 기능과 관련된 요청 변수 값이 잘못되었습니다. API 요청 프로토콜을 확인하시기 바랍니다."),
    INTERNAL_SERVER_ERROR("F500", "친구 목록에 대한 작업 중 알 수 없는 오류가 발생하였습니다.")
    ;

    private final String code;
    private final String msg;
}
