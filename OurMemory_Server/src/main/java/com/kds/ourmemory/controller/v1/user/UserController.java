package com.kds.ourmemory.controller.v1.user;

import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.user.dto.*;
import com.kds.ourmemory.service.v1.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.kds.ourmemory.controller.v1.ApiResult.ok;

@Api(tags = {"1. User"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/users")
public class UserController {
    private final UserService userService;

    @ApiOperation(value = "회원가입", notes = "앱에서 전달받은 데이터로 회원가입 진행")
    @PostMapping
    public ApiResult<InsertUserDto.Response> insert(@RequestBody InsertUserDto.Request request) {
        return ok(userService.signUp(request));
    }

    @ApiOperation(value = "로그인", notes = "SNS Id, 인증방식(snsType) 으로 사용자 정보 조회 및 리턴")
    @GetMapping(value = "/{snsId}/{snsType}")
    public ApiResult<SignInUserDto.Response> signIn(@ApiParam(value = "snsType", required = true) @PathVariable int snsType,
                                                    @ApiParam(value = "snsId", required = true) @PathVariable String snsId) {
        return ok(userService.signIn(snsType, snsId));
    }

    @ApiOperation(value = "내 정보 조회", notes = "내 정보를 모두 보여준다.")
    @GetMapping(value = "/{userId}")
    public ApiResult<FindUserDto.Response> find(@ApiParam(value = "userId", required = true) @PathVariable long userId) {
        return ok(userService.find(userId));
    }

    @ApiOperation(value = "사용자 검색", notes = "조건에 해당하는 사용자를 검색한다.")
    @GetMapping
    public ApiResult<List<FindUsersDto.Response>> findUsers(@ApiParam(value = "userId") @RequestParam(required = false) Long userId,
                                             @ApiParam(value = "name") @RequestParam(required = false) String name) {
        return ok(userService.findUsers(userId, name).stream()
                .map(FindUsersDto.Response::new)
                .collect(Collectors.toList()));
    }

    @ApiOperation(value = "푸시 토큰 수정", notes = "사용자 번호로 사용자를 찾아 푸시토큰 값을 수정한다.")
    @PatchMapping("/{userId}/token")
    public ApiResult<PatchTokenDto.Response> patchToken(
            @ApiParam(value = "userId", required = true) @PathVariable long userId,
            @RequestBody PatchTokenDto.Request request) {
        return ok(userService.patchToken(userId, request));
    }

    @ApiOperation(value = "사용자 정보 수정", notes = "전달받은 값이 있는 경우 수정한다.")
    @PutMapping("/{userId}")
    public ApiResult<PutUserDto.Response> update(
            @PathVariable long userId,
            @RequestBody PutUserDto.Request request
    ) {
        return ok(userService.update(userId, request));
    }

    @ApiOperation(value = "사용자 삭제", notes = "사용자 삭제 처리, 일정은 유지, 관계된 방에서 사용자 삭제/방장인 경우 방장 양도 후 삭제")
    @DeleteMapping(value = "/{userId}")
    public ApiResult<DeleteUserDto.Response> delete(@PathVariable long userId) {
        return ok(userService.delete(userId));
    }
}