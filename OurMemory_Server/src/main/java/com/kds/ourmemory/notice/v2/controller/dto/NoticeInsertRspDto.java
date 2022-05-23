package com.kds.ourmemory.notice.v2.controller.dto;

import com.kds.ourmemory.notice.v1.controller.dto.NoticeRspDto;
import com.kds.ourmemory.notice.v1.entity.Notice;
import com.kds.ourmemory.notice.v2.enums.NoticeType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@ApiModel(value = "NoticeInsertRspDto", description = "Insert Notice Response Dto")
@Getter
public class NoticeInsertRspDto {

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

    public NoticeInsertRspDto(NoticeRspDto noticeRspDto) {
        this.noticeId = noticeRspDto.getNoticeId();
        this.type = NoticeType.toV2(noticeRspDto.getType());
        this.value = noticeRspDto.getValue();
        this.read = noticeRspDto.isRead();
        this.regDate = noticeRspDto.getRegDate();
    }

}
