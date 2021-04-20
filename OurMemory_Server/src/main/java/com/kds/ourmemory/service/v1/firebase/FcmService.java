package com.kds.ourmemory.service.v1.firebase;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmiOSDto;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmService {
	private final String API_URL = "https://fcm.googleapis.com/v1/projects/our-memory-ed357/messages:send";
	private final ObjectMapper objectMapper;
	
	public void sendMessageTo(FcmRequestDto requestDto) {
		Response response = null;
		try {
			String message = makeMessage(requestDto.getToken(), requestDto.getTitle(), requestDto.getBody());
			
			OkHttpClient client = new OkHttpClient();
			RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
			Request request = new Request.Builder()
					.url(API_URL)
					.post(requestBody)
					.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
					.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
					.build();
			
			response = client.newCall(request).execute();
			
			log.debug(response.body().string());
		} catch (JsonProcessingException e) {
			log.error(e.toString());
			log.error("makeMessage Failed.");
		} catch (IOException e) {
			log.error(e.toString());
		} finally {
			Optional.ofNullable(response)
			    .ifPresent(rsp -> {
			        rsp.body().close();
                    rsp.close(); 
			    });
		}
	}
	
	private String makeMessage(String targetToken, String title, String body) throws JsonProcessingException {
		return objectMapper.writeValueAsString(new FcmiOSDto(targetToken, title, body, false));
	}
	
	private String getAccessToken() throws IOException {
		String firebaseConfigPath = "firebase/firebase_FCM_ServiceKey.json";
		
		GoogleCredentials googleCredentials = GoogleCredentials
				.fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
				.createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
		
		googleCredentials.refreshIfExpired();
		
		return googleCredentials.getAccessToken().getTokenValue();
	}
}
