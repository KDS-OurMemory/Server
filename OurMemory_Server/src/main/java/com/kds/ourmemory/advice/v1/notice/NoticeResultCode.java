package com.kds.ourmemory.advice.v1.notice;

import com.kds.ourmemory.advice.v1.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoticeResultCode implements ResultCode {
    /* Custom Error */
    NOT_FOUND_USER("N001", "알림 사용자 정보를 찾을 수 없습니다. 사용자 번호를 확인해주시기 바랍니다."),

    /* Http Status Error */
    BAD_REQUEST("N400", "알림 기능과 관련된 요청 변수 값이 잘못되었습니다. API 요청 프로토콜을 확인하시기 바랍니다."),
    NOT_FOUND("N404", "입력한 값에 해당하는 알림을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("N500", "알림에 대한 작업 중 알 수 없는 오류가 발생하였습니다.")
    ;

    private final String code;
    private final String msg;
}
