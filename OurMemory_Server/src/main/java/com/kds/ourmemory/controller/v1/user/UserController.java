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

import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.advice.v1.user.exception.UserTokenUpdateException;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchUserTokenRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchUserTokenResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.PutUserRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.PutUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.UserResponseDto;
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
    public ApiResult<InsertUserResponseDto> signUp(@RequestBody InsertUserRequestDto request)
            throws UserInternalServerException {
        return ok(service.signUp(request.toEntity()));
    }

    @ApiOperation(value = "로그인", notes = "userId 로 사용자 정보 조회 및 리턴")
    @GetMapping("/user")
    public ApiResult<UserResponseDto> signIn(
            @ApiParam(value = "snsId", required = true) @RequestParam String snsId,
            @ApiParam(value = "snsType", required = true) @RequestParam int snsType)
            throws UserNotFoundException {
        return ok(service.signIn(snsId, snsType));
    }

    @ApiOperation(value = "푸시 토큰 업데이트", notes = "userId 로 사용자를 찾아 푸시토큰 값을 업데이트한다.")
    @PatchMapping("/user/{userId}")
    public ApiResult<PatchUserTokenResponseDto> patchToken(
            @ApiParam(value = "userId", required = true) @PathVariable Long userId,
            @RequestBody PatchUserTokenRequestDto request) throws UserTokenUpdateException, UserNotFoundException {
        return ok(service.patchToken(userId, request));
    }
    
    @ApiOperation(value = "사용자 정보 업데이트", notes = "전달받은 값이 있는 경우 업데이트한다.")
    @PutMapping("/user/{userId}")
    public ApiResult<PutUserResponseDto> update(@ApiParam(value = "userId", required = true) @PathVariable Long userId,
            @RequestBody PutUserRequestDto request) {
        return ok(service.update(userId, request));
    }
}