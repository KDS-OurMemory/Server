package com.kds.ourmemory.dto.firebase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class FcmMessage {
	private boolean validate_only;
	private Message message;
	
	@Builder
	@AllArgsConstructor
	@Getter
	public static class Message {
		private String token;
//		private Notification notification;
		private Data data;
	}
	
	@Builder
	@AllArgsConstructor
	@Getter
	public static class Notification {
		private String title;
		private String body;
	}
	
	@Builder
	@AllArgsConstructor
	@Getter
	public static class Data {
		private String title;
		private String body;
	}
}
