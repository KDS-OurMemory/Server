package com.kds.ourmemory.controller.v1.user.dto;

import com.kds.ourmemory.entity.user.User;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InsertUserDto {
    
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @ApiModelProperty(value="SNS 인증방식", required = true, example = "1: 카카오, 2:구글, 3: 네이버")
        private Integer snsType;
        
        @ApiModelProperty(value="SNS 로그인 Id", required = true)
        private String snsId;
        
        @ApiModelProperty(value="FCM 토큰", required = true)
        private String pushToken;
        
        @ApiModelProperty(value="사용자명(또는 닉네임)", required = true)
        private String name;
        
        @ApiModelProperty(value="생일", required = false, example = "0717")
        private String birthday;
        
        @ApiModelProperty(value="양력 여부", required = false)
        private boolean solar;
        
        @ApiModelProperty(value="생일 공개여부", required = false)
        private boolean birthdayOpen;
        
        @ApiModelProperty(value="디바이스 OS", required = true)
        private String deviceOs;
        
        public User toEntity() {
            return User.builder()
                    .snsId(snsId)
                    .snsType(snsType)
                    .pushToken(pushToken)
                    .push(true)
                    .name(name)
                    .birthday(birthday)
                    .solar(solar)
                    .birthdayOpen(birthdayOpen)
                    .deviceOs(deviceOs)
                    .role("user")
                    .used(true)
                    .build();
        }
    }
    
    @Getter
    @AllArgsConstructor
    public static class Response {
        @ApiModelProperty(value = "사용자 번호")
        private Long userId;
        
        @ApiModelProperty(value = "사용자 추가한 날짜", notes = "yyyy-MM-dd HH:mm:ss", example = "2021-0420 11:03")
        private String joinDate;
    }
}
