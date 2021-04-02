package com.kds.ourmemory.advice.v1.user;

import com.kds.ourmemory.advice.v1.ResultCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserResultCode implements ResultCode{
    SUCCESS("00", "성공"),
    NOT_FOUND("U404", "입력한 값에 해당하는 회원을 찾을 수 없습니다."),
    PATCH_TOKEN_ERROR("U002", "FCM 토큰 업데이트 중 오류가 발생하였습니다."),
    INTERNAL_SERVER_ERROR("U500", "회원에 대한 작업 중 알 수 없는 오류가 발생하였습니다.")
    ;
    
    private String code;
    private String msg;
}
