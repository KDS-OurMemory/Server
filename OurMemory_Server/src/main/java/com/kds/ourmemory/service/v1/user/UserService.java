package com.kds.ourmemory.service.v1.user;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.kds.ourmemory.util.DateUtil.currentDate;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserResponseDto;
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

    @Transactional
    public InsertUserResponseDto signUp(User user) {
        return insertUser(user).map(u -> new InsertUserResponseDto(u.getId(), currentDate()))
                .orElseThrow(() -> new UserInternalServerException(
                        String.format("User '%s' insert failed.", user.getName())));
    }

    public UserResponseDto signIn(int snsType, String snsId) {
        checkArgument( 1<=snsType && snsType <=3, "지원하지 않는 SNS 인증방식입니다. 카카오(1), 구글(2), 네이버(3) 중에 선택해주시기 바랍니다.");
        checkNotNull(snsId, "snsId 값이 입력되지 않았습니다.");
        
        return findUser(snsType, snsId).map(UserResponseDto::new)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Not found user matched snsType '%d' and snsId '%s'.", snsType, snsId)));
    }

    @Transactional
    public PatchUserTokenResponseDto patchToken(long userId, String pushToken) {
        checkNotNull(pushToken, "토큰 값이 없거나 null입니다. 업데이트할 토큰 값을 확인해주시기 바랍니다.");
        
        return findUser(userId).map(user -> {
            user.changePushToken(pushToken);
            return new PatchUserTokenResponseDto(currentDate());
        }).orElseThrow(() -> new UserNotFoundException("Not found user matched to userId: " + userId));
    }

    @Transactional
    public PutUserResponseDto update(long userId, PutUserRequestDto request) {
        return findUser(userId).map(user -> {
            user.updateUser(request);
            return new PutUserResponseDto(currentDate());
        }).orElseThrow(() -> new UserNotFoundException("Not found user matched to userId: " + userId));
    }

    /**
     * User Repository
     */
    private Optional<User> insertUser(User user) {
        return Optional.ofNullable(userRepo.save(user));
    }

    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id).map(userRepo::findById).orElseGet(Optional::empty);
    }

    private Optional<User> findUser(int snsType, String snsId) {
        return Optional.ofNullable(snsId).map(sid -> userRepo.findBySnsIdAndSnsType(snsId, snsType))
                .orElseGet(Optional::empty);
    }
}
