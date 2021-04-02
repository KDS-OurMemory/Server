package com.kds.ourmemory.controller.v1.user;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kds.ourmemory.advice.v1.user.exception.UserInterServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.user.dto.DeleteUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchUserTokenResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchUserTokenRequestDto;
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
    public ApiResult<InsertUserResponseDto> signUp(@RequestBody InsertUserRequestDto request) {
        return ok(service.signUp(request.toEntity()));
    }

    @ApiOperation(value = "로그인", notes = "snsId 로 사용자 정보 조회 및 리턴")
    @GetMapping("/user/{snsId}")
    public ApiResult<UserResponseDto> signIn(@ApiParam(value = "snsId", required = true) @PathVariable String snsId)
            throws UserNotFoundException {
        return ok(service.signIn(snsId));
    }

    @ApiOperation(value = "푸시 토큰 업데이트", notes = "snsId 로 사용자를 찾아 푸시토큰 값을 업데이트한다.")
    @PatchMapping("/user/{snsId}")
    public ApiResult<PatchUserTokenResponseDto> patchToken(
            @ApiParam(value = "snsId", required = true) @PathVariable String snsId,
            @RequestBody PatchUserTokenRequestDto request) {
        return ok(service.patchToken(snsId, request));

    }

    @ApiOperation(value = "회원 삭제", notes = "아래 순서로 데이터 및 사용자가 삭제됩니다.\n\n 1. 사용자-방-일정 연결된 관계 삭제 \n 2. 사용자가 생성한 방/일정 삭제 \n 3. 사용자 삭제")
    @DeleteMapping("/user/{userId}")
    public ApiResult<DeleteUserResponseDto> delete(
            @ApiParam(value = "userId", required = true) @PathVariable Long userId) throws UserInterServerException {
        return ok(service.delete(userId));
    }
}