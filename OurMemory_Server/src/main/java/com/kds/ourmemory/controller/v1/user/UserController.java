package com.kds.ourmemory.controller.v1.user;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.user.dto.FindUserDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchTokenDto;
import com.kds.ourmemory.controller.v1.user.dto.PutUserDto;
import com.kds.ourmemory.service.v1.user.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@Api(tags = { "1. User" })
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class UserController {
    private final UserService service;

    @ApiOperation(value = "회원가입", notes = "앱에서 전달받은 데이터로 회원가입 진행")
    @PostMapping("/user")
    public ApiResult<InsertUserDto.Response> signUp(@RequestBody InsertUserDto.Request request) {
        return ok(service.signUp(request));
    }

    @ApiOperation(value = "로그인", notes = "SNS Id, 인증방식(snsType) 으로 사용자 정보 조회 및 리턴")
    @GetMapping("/user")
    public ApiResult<FindUserDto.Response> signIn(@ApiParam(value = "snsType", required = true) @RequestParam int snsType,
            @ApiParam(value = "snsId", required = true) @RequestParam String snsId) {
        return ok(service.signIn(snsType, snsId));
    }

    @ApiOperation(value = "푸시 토큰 업데이트", notes = "사용자 번호로 사용자를 찾아 푸시토큰 값을 업데이트한다.")
    @PatchMapping("/user/{userId}")
    public ApiResult<PatchTokenDto.Response> patchToken(
            @ApiParam(value = "userId", required = true) @PathVariable long userId,
            @RequestBody PatchTokenDto.Request request) {
        return ok(service.patchToken(userId, request));
    }

    @ApiOperation(value = "사용자 정보 업데이트", notes = "전달받은 값이 있는 경우 업데이트한다.")
    @PutMapping("/user/{userId}")
    public ApiResult<PutUserDto.Response> update(@ApiParam(value = "userId", required = true) @PathVariable long userId,
            @RequestBody PutUserDto.Request request) {
        return ok(service.update(userId, request));
    }
}