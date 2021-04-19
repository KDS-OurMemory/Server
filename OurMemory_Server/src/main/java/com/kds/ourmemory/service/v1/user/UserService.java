package com.kds.ourmemory.service.v1.user;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.kds.ourmemory.util.DateUtil.currentDate;

import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.user.dto.FindUserDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchTokenDto;
import com.kds.ourmemory.controller.v1.user.dto.PutUserDto;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepo;

    @Transactional
    public InsertUserDto.Response signUp(InsertUserDto.Request request) {
        checkNotNull(request.getName(), "이름이 입력되지 않았습니다. 이름을 입력해주세요.");
        checkArgument(StringUtils.isNoneBlank(request.getName()), "이름은 빈 값이 될 수 없습니다.");
        
        checkNotNull(request.getSnsType(), "SNS 인증방식(snsType)이 입력되지 않았습니다. 인증방식 값을 입력해주세요.");
        
        User user = request.toEntity();
        return insertUser(user).map(u -> new InsertUserDto.Response(u.getId(), currentDate()))
                .orElseThrow(() -> new UserInternalServerException(
                        String.format("User '%s' insert failed.", user.getName())));
    }

    public FindUserDto.Response signIn(int snsType, String snsId) {
        checkArgument(1 <= snsType && snsType <= 3, "지원하지 않는 SNS 인증방식입니다. 카카오(1), 구글(2), 네이버(3) 중에 입력해주시기 바랍니다.");
        checkArgument(StringUtils.isNoneBlank(snsId), "snsId 값은 빈 값이 될 수 없습니다.");

        return findUser(snsType, snsId).map(FindUserDto.Response::new)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Not found user matched snsType '%d' and snsId '%s'.", snsType, snsId)));
    }

    @Transactional
    public PatchTokenDto.Response patchToken(long userId, PatchTokenDto.Request request) {
        checkNotNull(request.getPushToken(), "토큰 값이 입력되지 않았습니다. 토큰 값을 입력해주세요.");
        checkArgument(StringUtils.isNoneBlank(request.getPushToken()), "토큰 값은 빈 값이 될 수 없습니다.");

        return findUser(userId).map(user -> {
            user.changePushToken(request.getPushToken());
            return new PatchTokenDto.Response(currentDate());
        }).orElseThrow(() -> new UserNotFoundException("Not found user matched to userId: " + userId));
    }

    @Transactional
    public PutUserDto.Response update(long userId, PutUserDto.Request request) {
        return findUser(userId).map(user -> {
            user.updateUser(request);
            return new PutUserDto.Response(currentDate());
        }).orElseThrow(() -> new UserNotFoundException("Not found user matched to userId: " + userId));
    }

    /**
     * User Repository
     */
    private Optional<User> insertUser(User user) {
        return Optional.ofNullable(userRepo.save(user));
    }

    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id)
                .map(userRepo::findById)
                .orElseGet(Optional::empty);
    }

    private Optional<User> findUser(int snsType, String snsId) {
        return Optional.ofNullable(snsId)
                .map(sid -> userRepo.findBySnsIdAndSnsType(snsId, snsType))
                .orElseGet(Optional::empty);
    }
}
