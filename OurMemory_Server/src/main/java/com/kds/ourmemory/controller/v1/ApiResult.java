package com.kds.ourmemory.controller.v1;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class ApiResult<T> {

    @ApiModelProperty(value = "API 요청 처리 결과", required = true)
    private final boolean success;

    @ApiModelProperty(value = "API 요청 처리 응답 값")
    private final T data;

    @ApiModelProperty(value = "API 요청 처리 오류 코드")
    private final String errorCode;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(true, data, "0");
    }

    public static ApiResult<String> error(String errorCode, String errorMessage) {
        return new ApiResult<>(false, errorMessage, errorCode);
    }
}
