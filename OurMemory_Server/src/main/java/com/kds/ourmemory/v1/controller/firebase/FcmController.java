package com.kds.ourmemory.v1.controller.firebase;

import com.kds.ourmemory.v1.controller.ApiResult;
import com.kds.ourmemory.v1.controller.firebase.dto.FcmReqDto;
import com.kds.ourmemory.v1.service.firebase.FcmService;
import com.kds.ourmemory.v1.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"7. Firebase"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/Firebase")
public class FcmController {

    private final FcmService fcmService;

    private final UserService userService;

    @ApiOperation(value = "FCM 메시지 전송", notes = "전송대상(FCM 토큰과 연결된 디바이스) 에게 푸시 메시지를 전달한다.")
    @PostMapping("/fcm")
    public ApiResult<Boolean> sendMessage(FcmReqDto reqDto) {
        var user = userService.find(reqDto.getUserId());
        return ApiResult.ok(fcmService.sendMessageTo(reqDto.toFcmDto(user)));
    }

}
