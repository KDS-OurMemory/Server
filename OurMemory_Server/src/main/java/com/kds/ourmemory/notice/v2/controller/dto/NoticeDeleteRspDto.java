package com.kds.ourmemory.notice.v2.controller.dto;

import com.kds.ourmemory.notice.v1.controller.dto.NoticeRspDto;
import io.swagger.annotations.ApiModel;
import lombok.Getter;

@ApiModel(value = "NoticeDeleteRspDto", description = "Delete Notice Response Dto")
@Getter
public class NoticeDeleteRspDto {

    public NoticeDeleteRspDto(NoticeRspDto noticeRspDto) {
    }

}
