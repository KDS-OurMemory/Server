package com.kds.ourmemory.user.v2.service;

import com.kds.ourmemory.user.v1.advice.exception.UserInternalServerException;
import com.kds.ourmemory.user.v1.advice.exception.UserNotFoundException;
import com.kds.ourmemory.user.v1.controller.dto.UserReqDto;
import com.kds.ourmemory.user.v1.controller.dto.UserRspDto;
import com.kds.ourmemory.user.v1.service.UserService;
import com.kds.ourmemory.user.v2.controller.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserV2Service {

    private final UserService userService;

    public UserSignUpRspDto signUp(UserSignUpReqDto reqDto) {
        return new UserSignUpRspDto(userService.signUp(reqDto.toDto()));
    }

    public UserSignInRspDto signIn(int snsType, String snsId) {
        return new UserSignInRspDto(userService.signIn(snsType, snsId));
    }

    public UserFindRspDto find(long userId) {
        return new UserFindRspDto(userService.find(userId));
    }

    public UserPatchTokenRspDto patchToken(long userId, UserPatchTokenReqDto reqDto) {
        return new UserPatchTokenRspDto(userService.patchToken(userId, reqDto.toDto()));
    }

    public UserUpdateRspDto update(long userId, UserUpdateReqDto reqDto) {
        return new UserUpdateRspDto(userService.update(userId, reqDto.toDto()));
    }

    public UserUploadProfileImageRspDto uploadProfileImage(long userId, UserUploadProfileImageReqDto reqDto) {
        return new UserUploadProfileImageRspDto(userService.uploadProfileImage(userId, reqDto.toDto()));
    }

    public UserDeleteProfileImageRspDto deleteProfileImage(long userId) {
        return new UserDeleteProfileImageRspDto(userService.deleteProfileImage(userId));
    }

    public UserDeleteRspDto delete(long userId) {
        return new UserDeleteRspDto(userService.delete(userId));
    }

}
