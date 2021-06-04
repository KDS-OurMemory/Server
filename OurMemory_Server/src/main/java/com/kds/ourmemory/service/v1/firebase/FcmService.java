package com.kds.ourmemory.service.v1.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import com.kds.ourmemory.config.CustomConfig;
import com.kds.ourmemory.controller.v1.firebase.dto.FcmDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmService {
	private final ObjectMapper objectMapper;
	private final CustomConfig customConfig;

	public void sendMessageTo(FcmDto.Request requestDto) {
		Response response = null;
		try {
			String message = makeMessage(requestDto);
			
			OkHttpClient client = new OkHttpClient();
			RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
			Request request = new Request.Builder()
					.url("https://fcm.googleapis.com/v1/projects/our-memory-ed357/messages:send")
					.post(requestBody)
					.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
					.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
					.build();
			
			response = client.newCall(request).execute();
			log.debug(Objects.requireNonNull(response.body()).string());
		} catch (JsonProcessingException e) {
			log.error(e.toString());
			log.error("makeMessage Failed.");
		} catch (IOException e) {
			log.error(e.toString());
		} finally {
			Optional.ofNullable(response)
			    .ifPresent(rsp -> {
			        Optional.ofNullable(rsp.body()).ifPresent(ResponseBody::close);
                    rsp.close();
			    });
		}
	}

	private String makeMessage(FcmDto.Request requestDto) throws JsonProcessingException {
		Object pushData = StringUtils.equals(requestDto.getDeviceOs(), FcmDto.DeviceOs.iOS.getType())?
				new FcmDto.RequestiOS(requestDto)
				: new FcmDto.RequestAndroid(requestDto);

		return objectMapper.writeValueAsString(pushData);
	}
	
	private String getAccessToken() throws IOException {
		GoogleCredentials googleCredentials = GoogleCredentials
				.fromStream(new FileInputStream(customConfig.getFcmKey()))
				.createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
		
		googleCredentials.refreshIfExpired();
		
		return googleCredentials.getAccessToken().getTokenValue();
	}
}
