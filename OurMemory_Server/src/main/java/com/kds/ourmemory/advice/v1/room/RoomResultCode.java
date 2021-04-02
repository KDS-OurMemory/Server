package com.kds.ourmemory.advice.v1.room;

import com.kds.ourmemory.advice.v1.ResultCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomResultCode implements ResultCode{
    NOT_FOUND("R404", "입력한 값에 해당하는 방을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("R500", "방에 대한 작업 중 알 수 없는 오류가 발생하였습니다.")
    ;
    private String code;
    private String msg;
}
