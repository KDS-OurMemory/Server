package com.kds.ourmemory.service.v1.user;

import static com.kds.ourmemory.util.DateUtil.currentDate;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
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
		return insertUser(user)
		        .map(u -> new InsertUserResponseDto(u.getId(), currentDate()))
                .orElseThrow(() -> new UserInternalServerException(
                        String.format("User '%s' insert failed.", user.getName())));
	}

    public UserResponseDto signIn(int snsType, String snsId) throws UserNotFoundException {
        return findUser(snsType, snsId)
                .map(UserResponseDto::new)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Not found user matched snsType '%d' and snsId '%s'.", snsType, snsId)));
    }
	
    @Transactional
    public PatchUserTokenResponseDto patchToken(Long userId, PatchUserTokenRequestDto request)
            throws UserNotFoundException {
        return findUser(userId).map(user -> {
            user.changePushToken(request.getPushToken());
            return new PatchUserTokenResponseDto(currentDate());
        })
        .orElseThrow(() -> new UserNotFoundException("Not found user matched to userId: " + userId));
    }
    
    @Transactional
    public PutUserResponseDto update(Long userId, PutUserRequestDto request) throws UserNotFoundException{
        return findUser(userId)
                .map(user -> {
                    user.updateUser(request);
                    return new PutUserResponseDto(currentDate());
                })
                .orElseThrow(() -> new UserNotFoundException("Not found user matched to userId: " + userId));
    }
    
    /**
     * User Repository
     */
    private Optional<User> insertUser(User user)  {
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
