package com.kds.ourmemory.controller.v1;

import com.kds.ourmemory.advice.v1.common.CommonResultCode;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class ApiResult<T> {
    @ApiModelProperty(value = "API 요청 처리 결과 코드", example = "0: 성공, 그 외: 오류 코드")
    private final String resultcode;
    
    @ApiModelProperty(value = "처리 결과 메시지")
    private final String message;
    
    @ApiModelProperty(value = "API 요청 처리 응답 값")
    private final T response;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(CommonResultCode.SUCCESS.getCode(), CommonResultCode.SUCCESS.getMsg(), data);
    }

    public static ApiResult<String> error(String errorCode, String errorMessage) {
        return new ApiResult<>(errorCode, errorMessage, null);
    }
}
