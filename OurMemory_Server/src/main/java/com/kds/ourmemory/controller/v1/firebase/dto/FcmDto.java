package com.kds.ourmemory.controller.v1.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class FcmDto {

    @AllArgsConstructor
    @Getter
    public static class Request {
        private final String token;
        private final String deviceOs;  // iOS, Android
        private final String title;
        private final String body;
    }

    @Getter
    public static class RequestAndroid {
        private final boolean validate_only;
        private final FcmDto.RequestAndroid.Message message;

        public RequestAndroid(String token, String title, String body, boolean validate_only) {

            FcmDto.RequestAndroid.Data data = new FcmDto.RequestAndroid.Data(title, body);
            message = new FcmDto.RequestAndroid.Message(data, token);
            this.validate_only = validate_only;
        }

        @AllArgsConstructor
        @Getter
        public class Message {
            private final FcmDto.RequestAndroid.Data data;
            private final String token;
        }

        @AllArgsConstructor
        @Getter
        public class Data {
            private final String title;
            private final String body;
        }
    }

    @Getter
    public static class RequestiOS {
        private final boolean validate_only;
        private final FcmDto.RequestiOS.Message message;

        public RequestiOS(String token, String title, String body, boolean validate_only) {

            FcmDto.RequestiOS.Notification notification = new FcmDto.RequestiOS.Notification(title, body, "MA4");  // 사운드는 문서참고하여 고정함.
            message = new FcmDto.RequestiOS.Message(notification, token);
            this.validate_only = validate_only;
        }

        @AllArgsConstructor
        @Getter
        public class Message {
            private final FcmDto.RequestiOS.Notification notification;
            private final String token;
        }

        @AllArgsConstructor
        @Getter
        public class Notification {
            private final String title;
            private final String body;
            private final String sound;
        }
    }
}
