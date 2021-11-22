package com.kds.ourmemory.controller.v1.notice.dto;

import com.kds.ourmemory.entity.notice.Notice;
import com.kds.ourmemory.entity.notice.NoticeType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@ApiModel(value = "NoticeDto", description = "Notice API Dto")
@Getter
public class NoticeDto {

    @ApiModelProperty(value = "알림 번호")
    private final long noticeId;

    @ApiModelProperty(value = "알림 종류", example = "FRIEND_REQUEST")
    private final NoticeType type;

    @ApiModelProperty(value = "알림 문자열 값", example = "99")
    private final String value;

    @ApiModelProperty(value = "알림 읽음 여부", example = "true")
    private final boolean read;

    @ApiModelProperty(value = "알림 생성 날짜", notes = "yyyy-MM-dd HH:mm:ss")
    private final String regDate;

    public NoticeDto(Notice notice) {
        this.noticeId = notice.getId();
        this.type = notice.getType();
        this.value = notice.getValue();
        this.read = notice.isRead();
        this.regDate = notice.formatRegDate();
    }

}
