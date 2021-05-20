package com.kds.ourmemory.controller.v1.firebase.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FcmDto {

    @ApiModel(value = "Fcm.Request", description = "nested class in FcmDto")
    @AllArgsConstructor
    @Getter
    public static class Request {
        private final String token;
        private final String deviceOs;  // iOS, Android
        private final String title;
        private final String body;
    }

    @ApiModel(value = "Fcm.RequestAndroid", description = "nested class in FcmDto")
    @Getter
    public static class RequestAndroid {
        @ApiModelProperty(value = "테스트 요청 플래그", required = true)
        private final boolean validate_only;
        private final Message message;

        public RequestAndroid(String token, String title, String body, boolean validate_only) {
            message = new Message(new FcmDto.RequestAndroid.Data(title, body), token);
            this.validate_only = validate_only;
        }

        @AllArgsConstructor
        @Getter
        private class Message {
            private final FcmDto.RequestAndroid.Data data;
            @ApiModelProperty(value = "FCM 전용 디바이스 토큰 값", required = true)
            private final String token;
        }

        @AllArgsConstructor
        @Getter
        private class Data {
            @ApiModelProperty(value = "제목", required = true)
            private final String title;
            @ApiModelProperty(value = "내용", required = true)
            private final String body;
        }
    }

    @ApiModel(value = "Fcm.RequestiOS", description = "nested class in FcmDto")
    @Getter
    public static class RequestiOS {
        @ApiModelProperty(value = "테스트 요청 플래그", required = true)
        private final boolean validate_only;
        private final Message message;

        public RequestiOS(String token, String title, String body, boolean validate_only) {
            message = new Message(new FcmDto.RequestiOS.Notification(title, body), token);
            this.validate_only = validate_only;
        }

        @AllArgsConstructor
        @Getter
        private class Message {
            private final FcmDto.RequestiOS.Notification notification;
            @ApiModelProperty(value = "FCM 전용 디바이스 토큰 값", required = true)
            private final String token;
        }

        @AllArgsConstructor
        @Getter
        private class Notification {
            @ApiModelProperty(value = "제목", required = true)
            private final String title;
            @ApiModelProperty(value = "내용", required = true)
            private final String body;
        }
    }

    @ApiModel(value = "Fcm.DeviceOs", description = "enum class in FcmDto")
    @Getter
    @AllArgsConstructor
    public enum DeviceOs {
        Android("Android"),
        iOS("iOS");

        private final String type;
    }
}
