package com.kds.ourmemory.controller.v1.notice.dto;

import com.kds.ourmemory.entity.notice.Notice;
import com.kds.ourmemory.entity.notice.NoticeType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindNoticesDto {

    @ApiModel(value = "FindNotices.Response", description = "nested class in FindNoticesDto")
    @Getter
    public static class Response {
        @ApiModelProperty(value = "알림 번호")
        private final long noticeId;

        @ApiModelProperty(value = "알림 종류", example = "friend_request")
        private final NoticeType type;

        @ApiModelProperty(value = "알림 문자열 값", example = "99")
        private final String value;

        @ApiModelProperty(value = "알림 생성 날짜", notes = "yyyy-MM-dd HH:mm:ss")
        private final String regDate;

        public Response(Notice notice) {
            this.noticeId = notice.getId();
            this.type = notice.getType();
            this.value = notice.getValue();
            this.regDate = notice.formatRegDate();
        }
    }
}
