package com.kds.ourmemory.service.v1.user;

import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.user.dto.*;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepo;

    private static final LongFunction<String> getUserNotFoundMessage = id -> "Not found user matched to userId: " + id;

    @Transactional
    public InsertUserDto.Response signUp(InsertUserDto.Request request) {
        var user = request.toEntity();
        return insertUser(user).map(u -> new InsertUserDto.Response(u.getId(), u.formatRegDate()))
                .orElseThrow(() -> new UserInternalServerException(
                        String.format("User '%s' insert failed.", user.getName())));
    }

    public SignInUserDto.Response signIn(int snsType, String snsId) {
        return findUser(snsType, snsId).map(SignInUserDto.Response::new)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Not found user matched snsType '%d' and snsId '%s'.", snsType, snsId)));
    }

    public FindUserDto.Response find(long userId) {
        return findUser(userId)
                .map(FindUserDto.Response::new)
                .orElseThrow(() -> new UserNotFoundException(getUserNotFoundMessage.apply(userId)));
    }

    public List<User> findUsers(Long userId, String name) {
        return findUsersByIdOrName(userId, name)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Not found users matched id '%d' or name '%s'", userId, name)));
    }

    public PatchTokenDto.Response patchToken(long userId, PatchTokenDto.Request request) {
        return findUser(userId).map(user ->
                user.changePushToken(request.getPushToken())
                        .map(u -> new PatchTokenDto.Response(u.formatModDate()))
                        .orElseThrow(() -> new UserInternalServerException("Failed to patch for user token."))
        )
       .orElseThrow(() -> new UserNotFoundException(getUserNotFoundMessage.apply(userId)));
    }

    public PutUserDto.Response update(long userId, PutUserDto.Request request) {
        return findUser(userId).map(user ->
                user.updateUser(request)
                        .map(u -> new PutUserDto.Response(u.formatModDate()))
                        .orElseThrow(() -> new UserInternalServerException("Failed to update for user data."))
        )
        .orElseThrow(() -> new UserNotFoundException(getUserNotFoundMessage.apply(userId)));
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
                .filter(users -> users.isPresent() && !users.get().isEmpty())
                .orElseGet(Optional::empty);
    }
}
