package com.kds.ourmemory.v1.controller;

import com.kds.ourmemory.v1.entity.BaseTimeEntity;
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
    private final String resultCode;
    
    @ApiModelProperty(value = "처리 결과 메시지")
    private final String message;

    @ApiModelProperty(value = "응답 시간", notes = "yyyy-MM-dd HH:mm")
    private final String responseDate;
    
    @ApiModelProperty(value = "API 요청 처리 응답 값")
    private final T response;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(
                "S00",
                "성공",
                BaseTimeEntity.formatNow(),
                data
        );
    }

    public static ApiResult<String> error(String errorCode, String errorMessage) {
        return new ApiResult<>(errorCode, errorMessage, BaseTimeEntity.formatNow(), null);
    }
}
