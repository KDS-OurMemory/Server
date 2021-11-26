package com.kds.ourmemory.controller.v1.notice.dto;

import com.kds.ourmemory.entity.notice.NoticeType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel(value = "NoticeReqDto", description = "Notice API Request Dto")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeReqDto {

    @ApiModelProperty(value = "사용자 번호", required = true)
    private Long userId;

    @ApiModelProperty(value = "알림 종류", required = true)
    private NoticeType noticeType;

    @ApiModelProperty(value = "알림 문자열 값", required = true)
    private String value;

}
