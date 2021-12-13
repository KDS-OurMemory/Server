package com.kds.ourmemory.v1.controller.notice.dto;

import com.kds.ourmemory.v1.entity.notice.Notice;
import com.kds.ourmemory.v1.entity.notice.NoticeType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@ApiModel(value = "NoticeRspDto", description = "Notice API Response Dto")
@Getter
public class NoticeRspDto {

    @ApiModelProperty(value = "알림 번호", required = true)
    private final long noticeId;

    @ApiModelProperty(value = "알림 종류(FRIEND_REQUEST: 친구 요청)", required = true, example = "FRIEND_REQUEST")
    private final NoticeType type;

    @ApiModelProperty(value = "알림 문자열 값", required = true, example = "99")
    private final String value;

    @ApiModelProperty(value = "알림 읽음 여부", required = true, example = "true")
    private final boolean read;

    @ApiModelProperty(value = "알림 생성 날짜(yyyy-MM-dd HH:mm:ss)", required = true)
    private final String regDate;

    public NoticeRspDto(Notice notice) {
        this.noticeId = notice.getId();
        this.type = notice.getType();
        this.value = notice.getValue();
        this.read = notice.isRead();
        this.regDate = notice.formatRegDate();
    }

}
