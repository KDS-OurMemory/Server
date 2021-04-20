package com.kds.ourmemory.controller.v1.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class FcmiOSDto {
	private boolean validate_only;
	private Message message;
	
	public FcmiOSDto(String token, String title, String body, boolean validate_only) {
	    
	    Notification notification = new Notification(title, body, "MA4");  // 사운드는 문서참고하여 고정함.
	    message = new Message(notification, token);
	    this.validate_only = validate_only;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Message {
		private Notification notification;
	    private String token;
	}
	
	@AllArgsConstructor
    @Getter
    public static class Notification {
        private String title;
        private String body;
        private String sound;
    }
}
