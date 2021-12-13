package com.kds.ourmemory.v1.service.firebase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import com.kds.ourmemory.v1.config.CustomConfig;
import com.kds.ourmemory.v1.controller.dto.FcmDto;
import com.kds.ourmemory.v1.entity.user.DeviceOs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
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

	public boolean sendMessageTo(FcmDto.Request requestDto) {
		Response response = null;
		var isSend = false;
		try {
			String message = makeMessage(requestDto);
			
			var client = new OkHttpClient();
			var requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
			var request = new Request.Builder()
					.url("https://fcm.googleapis.com/v1/projects/our-memory-ed357/messages:send")
					.post(requestBody)
					.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
					.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
					.build();
			
			response = client.newCall(request).execute();
			log.debug(Objects.requireNonNull(response.body()).string());
			isSend = true;
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

		return isSend;
	}

	private String makeMessage(FcmDto.Request requestDto) throws JsonProcessingException {
		Object pushData = StringUtils.equals(requestDto.getDeviceOs().getType(), DeviceOs.IOS.getType())?
				new FcmDto.RequestiOS(requestDto)
				: new FcmDto.RequestAndroid(requestDto);

		return objectMapper.writeValueAsString(pushData);
	}
	
	private String getAccessToken() throws IOException {
		var googleCredentials = GoogleCredentials
				.fromStream(new FileInputStream(customConfig.getFcmKey()))
				.createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
		
		googleCredentials.refreshIfExpired();
		
		return googleCredentials.getAccessToken().getTokenValue();
	}
}
