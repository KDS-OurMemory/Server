package com.kds.ourmemory.advice.v1.todo;

import com.kds.ourmemory.advice.v1.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TodoResultCode implements ResultCode {
    /* Custom Error */
    // TODO: 로직 생성 후 커스텀 추가


    /* Http Status Error */
    BAD_REQUEST("T400", "TODO 리스트 기능과 관련된 요청 변수 값이 잘못되었습니다. API 요청 프로토콜을 확인하시기 바랍니다."),
    NOT_FOUND("T404", "입력한 값에 해당하는 TODO 리스트를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("T500", "TODO 리스트에 대한 작업 중 알 수 없는 오류가 발생하였습니다.")
    ;
    private final String code;
    private final String msg;
}
