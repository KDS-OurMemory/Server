package com.kds.ourmemory.service.v1.firebase;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import com.kds.ourmemory.dto.firebase.FcmMessage;

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
public class FirebaseCloudMessageService {
	private final String API_URL = "https://fcm.googleapis.com/v1/projects/our-memory-ed357/messages:send";
	private final ObjectMapper objectMapper;
	
	public void sendMessageTo(String targetToken, String title, String body) {
		Response response = null;
		try {
			String message = makeMessage(targetToken, title, body);
			
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
			Consumer<Response> bodyClose = rsp -> Optional.ofNullable(rsp).ifPresent(r -> r.body().close());
			Consumer<Response> responseClose = rsp -> Optional.ofNullable(rsp).ifPresent(r -> r.close());
			
			bodyClose.accept(response);
			responseClose.accept(response);
		}
	}
	
	private String makeMessage(String targetToken, String title, String body) throws JsonProcessingException {
		FcmMessage fcsMessage = FcmMessage.builder()
				.message(FcmMessage.Message.builder()
					.token(targetToken)
//					.notification(FcmMessage.Notification.builder()
//						.title(title)
//						.body(body)
//						.build()
//					)
					.data(FcmMessage.Data.builder()
						.title(title)
						.body(body)
						.build()
					)
					.build()
				)
				.validate_only(false)
				.build();
		
		return objectMapper.writeValueAsString(fcsMessage);
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
