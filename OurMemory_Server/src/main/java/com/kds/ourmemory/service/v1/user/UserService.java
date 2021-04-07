package com.kds.ourmemory.service.v1.user;

import static com.kds.ourmemory.util.DateUtil.currentDate;

import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.advice.v1.user.exception.UserTokenUpdateException;
import com.kds.ourmemory.advice.v1.user.exception.UserUpdateException;
import com.kds.ourmemory.controller.v1.user.dto.DeleteUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchUserTokenRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchUserTokenResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.PutUserRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.PutUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.UserResponseDto;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.memory.MemoryService;
import com.kds.ourmemory.service.v1.room.RoomService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

    private final RoomService roomService;
    private final MemoryService memoryService;
    
	private final UserRepository userRepo;

	public InsertUserResponseDto signUp(User user) throws UserInternalServerException {
		return insert(user).map(u -> new InsertUserResponseDto(u.getId(), currentDate()))
		.orElseThrow(() -> new UserInternalServerException("signUP Failed."));
	}

	public UserResponseDto signIn(Long userId) throws UserNotFoundException {
		return findUserById(userId).map(UserResponseDto::new)
				.orElseThrow(() -> new UserNotFoundException("Not found user matched to userId: " + userId));
	}
	
    @Transactional
    public PatchUserTokenResponseDto patchToken(Long userId, PatchUserTokenRequestDto request)
            throws UserTokenUpdateException, UserNotFoundException {
        return Optional.ofNullable(request.getPushToken())
                .map(token -> {
                    findUserById(userId).get().changePushToken(token);
                    return new PatchUserTokenResponseDto(currentDate());
                })
                .orElseThrow(() -> new UserTokenUpdateException("Failed token update."));
    }
    
    public PutUserResponseDto update(Long userId, PutUserRequestDto request) throws UserUpdateException{
        return findUserById(userId)
                .map(user -> {
                    user.updateUser(request);
                    return new PutUserResponseDto(currentDate());
                })
                .orElseThrow(() -> new UserUpdateException("User update failed: " + userId));
    }
    
    /**
     * 사용자 삭제 기능 구현
     * 
     *  1. 사용자-방-일정 연결된 관계 삭제
     *  2. 사용자가 생성한 방,일정 삭제
     *  3. 사용자 삭제
     * 
     * 테스트로 생성한 사용자를 삭제하기 위해 임의 추가함
     * 
     * @param userId
     * @return DeleteUserResponseDto
     * @throws UserInternalServerException
     */
    @Transactional
    public DeleteUserResponseDto delete(Long userId) throws UserInternalServerException {
        return findUserById(userId).map(user -> {
            /* 
             * 내림차순으로 탐색
             * 오름차순으로 탐색할 경우, room 이 삭제되면 인덱스가 바뀌어서 이후 값들을 탐색하는데 오류가 발생한다.
             * */
            user.getRooms().stream()
                .collect(Collectors.toCollection(LinkedList::new)) 
                .descendingIterator()
                .forEachRemaining(room -> {
                    // 사용자가 생성한 방 삭제
                    Optional.of(room.getOwner())
                        .filter(user::equals)
                        .ifPresent(u -> roomService.delete(room.getId()));
    
                    // 사용자-방 관계 삭제
                    room.getUsers().remove(user);
                });

            /* 
             * 내림차순으로 탐색
             * 오름차순으로 탐색할 경우, memory 가 삭제되면 인덱스가 바뀌어서 이후 값들을 탐색하는데 오류가 발생한다.
             * */
            user.getMemorys().stream()
                .collect(Collectors.toCollection(LinkedList::new))
                .descendingIterator()
                .forEachRemaining(memory -> {
                    // 사용자가 생성한 일정 삭제
                    Optional.of(memory.getWriter())
                        .filter(user::equals)
                        .ifPresent(u -> memoryService.delete(memory.getId()));
    
                    // 사용자-일정 관계 삭제
                    memory.getUsers().remove(user);
                });
            
            // 사용자 삭제
            delete(user);
            return new DeleteUserResponseDto(currentDate());
        })
        .orElseThrow(() -> new UserInternalServerException("User Delete Failed: " + userId));
    }
	
	private Optional<User> insert(User user) {
	    return Optional.of(userRepo.save(user));
	}
	
	public Optional<User> findUserById(Long id) throws UserNotFoundException {
	    return userRepo.findById(id)
	            .map(Optional::of)
	            .orElseThrow(() -> new UserNotFoundException("Not found user matched to userId: " + id));
	}
	
	private void delete(User user) {
        userRepo.delete(user);
    }
}
