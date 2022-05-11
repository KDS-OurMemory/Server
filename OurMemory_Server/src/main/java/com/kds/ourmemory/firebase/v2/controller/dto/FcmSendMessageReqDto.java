package com.kds.ourmemory.firebase.v2.controller.dto;

import com.kds.ourmemory.firebase.v1.controller.dto.FcmDto;
import com.kds.ourmemory.user.v2.controller.dto.UserFindRspDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@ApiModel(value = "FcmSendMessageReqDto", description = "Send FCM Request Dto")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmSendMessageReqDto {

    @ApiModelProperty(value = "푸시 메시지 제목", required = true)
    private String title;

    @ApiModelProperty(value = "푸시 메시지 내용", required = true)
    private String body;

    public FcmDto.Request toDto(UserFindRspDto rspDto) {
        return FcmDto.Request.builder()
                .token(rspDto.getPushToken())
                .deviceOs(rspDto.getDeviceOs())
                .title(title)
                .body(body)
                .build();
    }

}
