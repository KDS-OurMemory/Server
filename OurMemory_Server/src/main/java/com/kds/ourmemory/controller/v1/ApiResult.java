package com.kds.ourmemory.controller.v1;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class ApiResult<T> {

    @ApiModelProperty(value = "API 요청 처리 결과", required = true)
    private final boolean success;

    @ApiModelProperty(value = "API 요청 처리 응답 값")
    private final T data;

    @ApiModelProperty(value = "API 요청 처리 상태 코드")
    private final int status;

    public static <T> ApiResult<T> ok(T data, HttpStatus status) {
        return new ApiResult<>(true, data, status.value());
    }

    public static ApiResult<?> error(String errorMessage, HttpStatus status) {
        return new ApiResult<>(false, errorMessage, status.value());
    }
}
