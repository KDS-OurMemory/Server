package com.kds.ourmemory.controller.v1.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class FcmAndroidDto {
    private boolean validate_only;
    private Message message;
    
    public FcmAndroidDto(String token, String title, String body, boolean validate_only) {
        
        Data data = new Data(title, body);
        message = new Message(data, token);
        this.validate_only = validate_only;
    }
    
    @AllArgsConstructor
    @Getter
    public static class Message {
        private Data data;
        private String token;
    }
    
    /* 안드로이드 백그라운드 푸시 전용 */
    @AllArgsConstructor
    @Getter
    public static class Data {
        private String title;
        private String body;
    }
}
