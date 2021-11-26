package com.kds.ourmemory.controller.v1.notice.dto;

import com.kds.ourmemory.entity.notice.Notice;
import com.kds.ourmemory.entity.notice.NoticeType;
import com.kds.ourmemory.entity.user.User;
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
    private String noticeValue;

    public Notice toEntity(User user) {
        return Notice.builder()
                .user(user)
                .type(noticeType)
                .value(noticeValue)
                .build();
    }

}
