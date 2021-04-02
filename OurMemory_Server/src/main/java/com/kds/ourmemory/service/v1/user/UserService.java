package com.kds.ourmemory.service.v1.user;

import static com.kds.ourmemory.util.DateUtil.currentDate;

import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.v1.user.exception.UserInterServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.advice.v1.user.exception.UserPatchTokenException;
import com.kds.ourmemory.controller.v1.user.dto.DeleteUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.InsertUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchUserTokenRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.PatchUserTokenResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.UserResponseDto;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.memory.MemoryService;
import com.kds.ourmemory.service.v1.room.RoomService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepo;
	
	// 사용자와 관련된 방을 작업하기 위해 추가
	private final RoomService roomService;
	
    // 사용자와 관련된 일정을 작업하기 위해 추가
	private final MemoryService memoryService;

	public InsertUserResponseDto signUp(User user) throws UserInterServerException {
		return insert(user).map(u -> new InsertUserResponseDto(currentDate()))
		.orElseThrow(() -> new UserInterServerException("signUP Failed."));
	}

	public UserResponseDto signIn(String snsId) throws UserNotFoundException {
		return findUserBySnsId(snsId).map(UserResponseDto::new)
				.orElseThrow(() -> new UserNotFoundException("Not found user matched to snsId: " + snsId));
	}
	
	@Transactional
    public PatchUserTokenResponseDto patchToken(String snsId, PatchUserTokenRequestDto request)
            throws UserPatchTokenException, UserNotFoundException {
        return Optional.ofNullable(request.getPushToken())
                .map(token -> findUserBySnsId(snsId).orElseThrow(() -> new UserNotFoundException("Not found user for snsId")))
                .map(user -> {
                    user.setPushToken(request.getPushToken());
                return new PatchUserTokenResponseDto(currentDate());
            })
            .orElseThrow(() -> new UserPatchTokenException("Failed token update."));
    }
	
	@Transactional
	public DeleteUserResponseDto delete(Long userId) throws UserInterServerException {
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
	    .orElseThrow(() -> new UserInterServerException("User Delete Failed: " + userId));
	}
	
	private Optional<User> insert(User user) {
	    return Optional.of(userRepo.save(user));
	}
	
	public Optional<User> findUserById(Long id) {
	    return userRepo.findById(id);
	}
	
	private Optional<User> findUserBySnsId(String snsId) {
	    return userRepo.findBySnsId(snsId);
	}
	
	private void delete(User user) {
        userRepo.delete(user);
    }
}
