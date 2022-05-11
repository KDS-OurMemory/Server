package com.kds.ourmemory.firebase.v2.controller;

import com.kds.ourmemory.common.v1.controller.ApiResult;
import com.kds.ourmemory.firebase.v2.controller.dto.FcmSendMessageReqDto;
import com.kds.ourmemory.firebase.v2.service.FcmV2Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = {"7-2. Firebase"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v2/Firebase")
public class FcmV2Controller {

    private final FcmV2Service fcmV2Service;

    @ApiOperation(value = "FCM 메시지 전송", notes = "전송대상(FCM 토큰과 연결된 디바이스) 에게 푸시 메시지를 전달한다.")
    @PostMapping("/fcm/users/{userId}")
    public ApiResult<Boolean> sendMessage(
            @ApiParam(value = "사용자 번호", required = true) @PathVariable long userId,
            @RequestBody FcmSendMessageReqDto reqDto
    ) {
        return ApiResult.ok(fcmV2Service.sendMessage(userId, reqDto));
    }

}
