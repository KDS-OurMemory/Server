package com.kds.ourmemory.firebase.v1.controller.dto;

import com.kds.ourmemory.user.v1.controller.dto.UserRspDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel(value = "FcmReqDto", description = "FCM 메시지 전송 요청 Dto")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmReqDto {

    @ApiModelProperty(value = "푸시 메시지 제목", required = true)
    private String title;

    @ApiModelProperty(value = "푸시 메시지 내용", required = true)
    private String body;

    public FcmDto.Request toFcmDto(UserRspDto userRspDto) {
        return FcmDto.Request.builder()
                .token(userRspDto.getPushToken())
                .deviceOs(userRspDto.getDeviceOs())
                .title(title)
                .body(body)
                .build();
    }

}
