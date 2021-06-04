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
        /* required field */
        @ApiModelProperty(value = "푸시대상 토큰", required = true)
        private final String token;

        @ApiModelProperty(value = "푸시대상 기기 종류", required = true, notes = "iOS | Android")
        private final String deviceOs;

        @ApiModelProperty(value = "푸시 메시지 제목", required = true)
        private final String title;

        @ApiModelProperty(value = "푸시 메시지 내용", required = true)
        private final String body;

        /* option field */
        @ApiModelProperty(value = "테스트 푸시 여부")
        private boolean isValidate;

        @ApiModelProperty(value = "푸시 메시지와 별도로 전송할 데이터 종류")
        private String dataType;

        @ApiModelProperty(value = "데이터", notes = "푸시 메시지와 별도로 전달할 데이터, 문자열만 가능함.")
        private String dataString;

        // constructor for required field only
        public Request(String token, String deviceOs, String title, String body) {
            this.token = token;
            this.deviceOs = deviceOs;
            this.title = title;
            this.body = body;
        }
    }

    @ApiModel(value = "Fcm.RequestAndroid", description = "nested class in FcmDto")
    @Getter
    public static class RequestAndroid {
        @ApiModelProperty(value = "푸시 테스트 여부", required = true)
        private final boolean validate_only;

        private final Message message;

        public RequestAndroid(FcmDto.Request request) {
            message = new Message(request.token,
                    new FcmDto.RequestAndroid.Data(request.title, request.body, request.dataType, request.dataString));
            this.validate_only = request.isValidate;
        }

        @AllArgsConstructor
        @Getter
        private class Message {
            @ApiModelProperty(value = "FCM 전용 디바이스 토큰 값", required = true)
            private final String token;

            private final FcmDto.RequestAndroid.Data data;
        }

        @AllArgsConstructor
        @Getter
        private class Data {
            @ApiModelProperty(value = "제목", required = true, notes = "푸시 메시지 제목")
            private final String title;

            @ApiModelProperty(value = "내용", required = true, notes = "푸시 메시지 내용")
            private final String body;

            @ApiModelProperty(value = "종류", notes = "푸시 메시지와 별도로 전달할 데이터 종류")
            private final String dataType;

            @ApiModelProperty(value = "데이터", notes = "푸시 메시지와 별도로 전달할 데이터, 문자열만 가능")
            private final String dataString;
        }
    }

    @ApiModel(value = "Fcm.RequestiOS", description = "nested class in FcmDto")
    @Getter
    public static class RequestiOS {
        @ApiModelProperty(value = "테스트 요청 플래그", required = true)
        private final boolean validate_only;
        private final Message message;

        public RequestiOS(FcmDto.Request request) {
            message = new Message(request.token,
                    new Notification(request.title, request.body),
                    new Data(request.dataType, request.dataString));
            this.validate_only = request.isValidate;
        }

        @AllArgsConstructor
        @Getter
        private class Message {
            @ApiModelProperty(value = "FCM 전용 디바이스 토큰 값", required = true)
            private final String token;

            private final Notification notification;
            private final Data data;
        }

        @AllArgsConstructor
        @Getter
        private class Notification {
            @ApiModelProperty(value = "제목", required = true)
            private final String title;
            @ApiModelProperty(value = "내용", required = true)
            private final String body;
        }

        @AllArgsConstructor
        @Getter
        private class Data {
            @ApiModelProperty(value = "종류", notes = "푸시 메시지와 별도로 전달할 데이터 종류")
            private final String dataType;

            @ApiModelProperty(value = "데이터", notes = "푸시 메시지와 별도로 전달할 데이터, 문자열만 가능")
            private final Object dataObj;
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
