package com.kds.ourmemory.notice.v2.controller.dto;

import com.kds.ourmemory.notice.v1.controller.dto.NoticeReqDto;
import com.kds.ourmemory.notice.v1.entity.Notice;
import com.kds.ourmemory.notice.v1.entity.NoticeType;
import com.kds.ourmemory.user.v1.entity.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ApiModel(value = "NoticeInsertReqDto", description = "Insert Notice Request Dto")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeInsertReqDto {

    @ApiModelProperty(value = "사용자 번호", required = true)
    private Long userId;

    @ApiModelProperty(value = "알림 종류", required = true)
    private NoticeType noticeType;

    @ApiModelProperty(value = "알림 문자열 값", required = true)
    private String noticeValue;

    public NoticeReqDto toDto() {
        return new NoticeReqDto(userId, noticeType, noticeValue);
    }

}
