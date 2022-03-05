package com.kds.ourmemory.v1.controller.user;

import com.kds.ourmemory.v1.controller.ApiResult;
import com.kds.ourmemory.v1.controller.user.dto.UserReqDto;
import com.kds.ourmemory.v1.controller.user.dto.UserRspDto;
import com.kds.ourmemory.v1.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static com.kds.ourmemory.v1.controller.ApiResult.ok;

@Api(tags = {"1. User"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/users")
public class UserController {
    private final UserService userService;

    @ApiOperation(value = "회원가입", notes = "앱에서 전달받은 데이터로 회원가입 진행")
    @PostMapping
    public ApiResult<UserRspDto> insert(@RequestBody UserReqDto reqDto) {
        return ok(userService.signUp(reqDto));
    }

    @ApiOperation(value = "로그인", notes = """
            sns 종류와 SNS Id 로 로그인한다.
            사용자가 조회되지 않는 경우 비회원 예외코드(U005)를 리턴한다.""")
    @GetMapping
    public ApiResult<UserRspDto> signIn(
            @ApiParam(value = "sns 종류", required = true) @RequestParam int snsType,
            @ApiParam(value = "snsId", required = true) @RequestParam String snsId
    ) {
        return ok(userService.signIn(snsType, snsId));
    }

    @ApiOperation(value = "내 정보 조회", notes = "내 정보를 모두 보여준다.")
    @GetMapping("/{userId}")
    public ApiResult<UserRspDto> find(
            @ApiParam(value = "사용자 번호", required = true) @PathVariable long userId
    ) {
        return ok(userService.find(userId));
    }

    @ApiOperation(value = "푸시 토큰 수정", notes = "사용자 번호로 사용자를 찾아 푸시토큰 값을 수정한다.")
    @PatchMapping("/{userId}/token")
    public ApiResult<UserRspDto> patchToken(
            @ApiParam(value = "사용자 번호", required = true) @PathVariable long userId,
            @RequestBody UserReqDto reqDto) {
        return ok(userService.patchToken(userId, reqDto));
    }

    @ApiOperation(value = "사용자 정보 수정", notes = "전달받은 값이 있는 경우 수정한다.")
    @PutMapping("/{userId}")
    public ApiResult<UserRspDto> update(
            @ApiParam(value = "사용자 번호", required = true) @PathVariable long userId,
            @RequestBody UserReqDto reqDto
    ) {
        return ok(userService.update(userId, reqDto));
    }

    @ApiOperation(value = "프로필사진 업로드", notes = "프로필 사진을 업로드한다. 1개만 업로드가능하며, 새로 업로드할 경우 이전 파일은 삭제된다.")
    @PostMapping(value = "/{userId}/profileImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult<UserRspDto> uploadProfileImage(
            @ApiParam(value = "사용자 번호", required = true) @PathVariable long userId,
            UserReqDto reqDto
    ) {
        return ok(userService.uploadProfileImage(userId, reqDto));
    }

    @ApiOperation(value = "프로필사진 삭제", notes = "프로필 사진을 삭제한다.")
    @DeleteMapping("/{userId}/profileImage")
    public ApiResult<UserRspDto> deleteProfileImage(
            @ApiParam(value = "사용자 번호", required = true) @PathVariable long userId
    ) {
        return ok(userService.deleteProfileImage(userId));
    }

    @ApiOperation(value = "사용자 삭제", notes = """
        사용자 삭제 처리, 일정은 유지, 관계된 방에서 사용자 삭제/방장인 경우 방장 양도 후 삭제\s
        성공한 경우, 삭제 여부를 resultCode 로 전달하기 때문에 response=null 을 리턴한다.""")
    @DeleteMapping("/{userId}")
    public ApiResult<UserRspDto> delete(
            @ApiParam(value = "사용자 번호", required = true) @PathVariable long userId
    ) {
        return ok(userService.delete(userId));
    }
}