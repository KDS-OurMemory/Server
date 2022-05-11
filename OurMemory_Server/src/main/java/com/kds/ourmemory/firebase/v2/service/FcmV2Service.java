package com.kds.ourmemory.firebase.v2.service;

import com.kds.ourmemory.firebase.v1.service.FcmService;
import com.kds.ourmemory.firebase.v2.controller.dto.FcmSendMessageReqDto;
import com.kds.ourmemory.user.v2.service.UserV2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmV2Service {

    private final FcmService fcmService;

    private final UserV2Service userV2Service;

    public boolean sendMessage(long userId, FcmSendMessageReqDto reqDto) {
        var userFindRspDto = userV2Service.find(userId);
        return fcmService.sendMessageTo(reqDto.toDto(userFindRspDto));
    }

}
