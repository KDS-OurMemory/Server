package com.kds.ourmemory.service.v1.user;

import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.user.dto.*;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepo;

    @Transactional
    public InsertUserDto.Response signUp(InsertUserDto.Request request) {
        checkNotNull(request.getName(), "이름이 입력되지 않았습니다. 이름을 입력해주세요.");
        checkArgument(StringUtils.isNoneBlank(request.getName()), "이름은 빈 값이 될 수 없습니다.");

        checkNotNull(request.getSnsType(), "SNS 인증방식(snsType)이 입력되지 않았습니다. 인증방식 값을 입력해주세요.");

        checkArgument(StringUtils.isNoneBlank(request.getSnsId()), "SNS ID 는 빈 값이 될 수 없습니다.");

        User user = request.toEntity();
        return insertUser(user).map(u -> new InsertUserDto.Response(u.getId(), u.formatRegDate()))
                .orElseThrow(() -> new UserInternalServerException(
                        String.format("User '%s' insert failed.", user.getName())));
    }

    public FindUserDto.Response signIn(int snsType, String snsId) {
        checkArgument(1 <= snsType && snsType <= 3, "지원하지 않는 SNS 인증방식입니다. 카카오(1), 구글(2), 네이버(3) 중에 입력해주시기 바랍니다.");
        checkArgument(StringUtils.isNoneBlank(snsId), "SNS ID 는 빈 값이 될 수 없습니다.");

        return findUser(snsType, snsId).map(FindUserDto.Response::new)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Not found user matched snsType '%d' and snsId '%s'.", snsType, snsId)));
    }

    public FindUsersDto.Response findUsers(Long userId, String name) {
        return findUsersByIdOrName(userId, name)
                .map(FindUsersDto.Response::new)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Not found user matched id '%d' or name '%s'", userId, name)));
    }

    public PatchTokenDto.Response patchToken(long userId, PatchTokenDto.Request request) {
        checkNotNull(request.getPushToken(), "토큰 값이 입력되지 않았습니다. 토큰 값을 입력해주세요.");
        checkArgument(StringUtils.isNoneBlank(request.getPushToken()), "토큰 값은 빈 값이 될 수 없습니다.");

        return findUser(userId).map(user -> {
            user.changePushToken(request.getPushToken());

            return updateUser(user).map(u -> new PatchTokenDto.Response(u.formatModDate()))
                    .orElseThrow(() -> new UserInternalServerException("Failed to patch for user token."));
        }).orElseThrow(() -> new UserNotFoundException("Not found user matched to userId: " + userId));
    }

    public PutUserDto.Response update(long userId, PutUserDto.Request request) {
        return findUser(userId).map(user -> {
            user.updateUser(request);

            return updateUser(user).map(u -> new PutUserDto.Response(u.formatModDate()))
                    .orElseThrow(() -> new UserInternalServerException("Failed to update for user data."));
        }).orElseThrow(() -> new UserNotFoundException("Not found user matched to userId: " + userId));
    }

    /**
     * User Repository
     */
    private Optional<User> insertUser(User user) {
        return Optional.of(userRepo.save(user));
    }

    private Optional<User> findUser(Long id) {
        return Optional.ofNullable(id).flatMap(userRepo::findById);
    }

    private Optional<User> findUser(int snsType, String snsId) {
        return Optional.ofNullable(snsId).flatMap(sid -> userRepo.findBySnsIdAndSnsType(snsId, snsType));
    }

    private Optional<List<User>> findUsersByIdOrName(Long userId, String name) {
        return Optional.ofNullable(userRepo.findAllByIdOrName(userId, name))
                .orElseGet(Optional::empty);
    }

    private Optional<User> updateUser(User user) {
        return Optional.of(userRepo.save(user));
    }
}
