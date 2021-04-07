package com.kds.ourmemory.controller.v1.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class FcmMessageDto {
	private boolean validate_only;
	private Message message;
	
	public FcmMessageDto(String token, String title, String body, boolean validate_only) {
	    Data data = new Data(title, body);
	    message = new Message(token, data);
	    this.validate_only = validate_only;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Message {
		private String token;
		private Data data;
	}
	
	@AllArgsConstructor
	@Getter
	public static class Data {
		private String title;
		private String body;
	}
}
