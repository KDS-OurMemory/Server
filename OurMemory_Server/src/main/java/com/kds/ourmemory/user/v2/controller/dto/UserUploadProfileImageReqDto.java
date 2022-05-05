package com.kds.ourmemory.user.v2.controller.dto;

import com.kds.ourmemory.user.v1.controller.dto.UserReqDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@ApiModel(value = "UserUploadProfileImageReqDto", description = "Upload ProfileImage Request Dto")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserUploadProfileImageReqDto {

    @ApiModelProperty(value = "프로필 이미지 파일")
    private MultipartFile profileImage;

    public UserReqDto toDto() {
        return UserReqDto.builder()
                .profileImage(profileImage)
                .build();
    }

}
