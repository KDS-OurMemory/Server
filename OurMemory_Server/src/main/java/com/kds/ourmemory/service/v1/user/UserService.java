package com.kds.ourmemory.service.v1.user;

import static com.kds.ourmemory.util.DateUtil.currentDate;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.advice.v1.user.exception.UserTokenUpdateException;
import com.kds.ourmemory.advice.v1.user.exception.UserUpdateException;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchUserTokenRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchUserTokenResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.PutUserRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.PutUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.UserResponseDto;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
	private final UserRepository userRepo;

	public InsertUserResponseDto signUp(User user) throws UserInternalServerException {
		return userRepo.insertUser(user)
		        .map(u -> new InsertUserResponseDto(u.getId(), currentDate()))
		        .orElseThrow(() -> new UserInternalServerException("signUp Failed."));
	}

    public UserResponseDto signIn(String snsId, int snsType) throws UserNotFoundException, UserInternalServerException {
        return userRepo.findBySnsIdAndSnsType(snsId, snsType)
                .map(UserResponseDto::new)
                .orElseThrow(() -> new UserInternalServerException("User Found Failed: " + snsId));
    }
	
    @Transactional
    public PatchUserTokenResponseDto patchToken(Long userId, PatchUserTokenRequestDto request)
            throws UserTokenUpdateException, UserNotFoundException {
        return Optional.ofNullable(request.getPushToken())
                .map(token -> {
                    userRepo.findById(userId).get().changePushToken(token);
                    return new PatchUserTokenResponseDto(currentDate());
                })
                .orElseThrow(() -> new UserTokenUpdateException("Failed token update."));
    }
    
    @Transactional
    public PutUserResponseDto update(Long userId, PutUserRequestDto request) throws UserUpdateException{
        return userRepo.findById(userId)
                .map(user -> {
                    user.updateUser(request);
                    return new PutUserResponseDto(currentDate());
                })
                .orElseThrow(() -> new UserUpdateException("User update failed: " + userId));
    }
}
