package com.kds.ourmemory.service.v1.user;

import static com.kds.ourmemory.util.DateUtil.currentDate;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CUserException;
import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.controller.v1.user.dto.DeleteUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.SignUpResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.UserResponseDto;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FirebaseCloudMessageService;
import com.kds.ourmemory.service.v1.memory.MemoryService;
import com.kds.ourmemory.service.v1.room.RoomService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepo;
	private final FirebaseCloudMessageService firebaseFcm;
	
	// 사용자와 관련된 방을 작업하기 위해 추가
	private final RoomService roomService;
	
    // 사용자와 관련된 일정을 작업하기 위해 추가
	private final MemoryService memoryService;

	public SignUpResponseDto signUp(User user) throws CUserException {
		return insert(user).map(u -> {
            firebaseFcm.sendMessageTo(user.getPushToken(), "OurMemory - SignUp", user.getName() + " is SignUp Success");
	        return new SignUpResponseDto(currentDate());
		}).orElseThrow(() -> {
            firebaseFcm.sendMessageTo(user.getPushToken(), "OurMemory - SignUp", user.getName() + " is SignUp Failed.");
		    throw new CUserException("signUP Failed.");
		});
	}

	public UserResponseDto signIn(String snsId) throws CUserNotFoundException {
		return findUserBySnsId(snsId).map(UserResponseDto::new)
				.orElseThrow(() -> new CUserNotFoundException("Not found user matched to snsId: " + snsId));
	}
	
	@Transactional
	public DeleteUserResponseDto delete(Long userId) throws CUserException {
	    return findUserById(userId).map(user -> {
	        user.getRooms().stream()
	        .forEach(room -> {
	            // 사용자가 생성한 방 삭제
	            Optional.of(room.getOwner())
	                .filter(user::equals)
	                .ifPresent(u -> roomService.delete(room.getId()));

	            // 사용자-방 관계 삭제
	            room.getUsers().remove(user);
	            });
	        
	        user.getMemorys().stream()
	        .forEach(memory -> {
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
	    .orElseThrow(() -> new CUserException("User Delete Failed: " + userId));
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
