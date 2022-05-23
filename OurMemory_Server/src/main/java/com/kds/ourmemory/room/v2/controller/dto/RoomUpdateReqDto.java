package com.kds.ourmemory.room.v2.controller.dto;

import com.kds.ourmemory.room.v1.controller.dto.RoomReqDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@ApiModel(value = "RoomUpdateReqDto", description = "Update Room Request Dto")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomUpdateReqDto {

    @ApiModelProperty(value = "방 이름")
    private String name;

    @ApiModelProperty(value = "방 공개 여부")
    private boolean opened;

    public RoomReqDto toDto() {
        return RoomReqDto.builder()
                .name(name)
                .opened(opened)
                .build();
    }

}
