package com.kds.ourmemory.controller.v1.user;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.user.dto.SignInResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.SignUpRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.SignUpResponseDto;
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
    public ApiResult<SignUpResponseDto> signUp(@RequestBody SignUpRequestDto request) {
        return ok(service.signUp(request.toEntity()));
    }

    @ApiOperation(value = "로그인", notes = "snsId 로 사용자 정보 조회 및 리턴")
    @GetMapping("/user/{snsId}")
    public ApiResult<SignInResponseDto> signIn(@ApiParam(value = "snsId", required = true) @PathVariable String snsId)
            throws CUserNotFoundException {
        return ok(service.signIn(snsId));
    }
}