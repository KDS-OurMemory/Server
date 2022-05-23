package com.kds.ourmemory.room.v2.controller.dto;

import com.kds.ourmemory.room.v1.controller.dto.RoomReqDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@ApiModel(value = "RoomInsertReqDto", description = "Insert Room Request Dto")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomInsertReqDto {

    @ApiModelProperty(value = "방 이름")
    private String name;

    @ApiModelProperty(value = "사용자 번호", example = "50")
    private Long userId;

    @ApiModelProperty(value = "방 공개 여부")
    private boolean opened;

    @ApiModelProperty(value = "초대할 멤버", example = "[2,4]")
    private List<Long> member;

    public RoomReqDto toDto() {
        return RoomReqDto.builder()
                .name(name)
                .userId(userId)
                .opened(opened)
                .member(member)
                .build();
    }

}
