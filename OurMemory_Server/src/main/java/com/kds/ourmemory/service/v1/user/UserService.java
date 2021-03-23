package com.kds.ourmemory.service.v1.user;

import static com.kds.ourmemory.util.DateUtil.currentDate;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CUserException;
import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.controller.v1.user.dto.DeleteUserResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.SignInResponseDto;
import com.kds.ourmemory.controller.v1.user.dto.SignUpResponseDto;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FirebaseCloudMessageService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepo;
	private final FirebaseCloudMessageService firebaseFcm;

	public SignUpResponseDto signUp(User user) throws CUserException {
		return insert(user).map(u -> {
            firebaseFcm.sendMessageTo(user.getPushToken(), "OurMemory - SignUp", user.getName() + " is SignUp Success");
	        return new SignUpResponseDto(currentDate());
		}).orElseThrow(() -> {
            firebaseFcm.sendMessageTo(user.getPushToken(), "OurMemory - SignUp", user.getName() + " is SignUp Failed.");
		    throw new CUserException("signUP Failed.");
		});
	}

	public SignInResponseDto signIn(String snsId) throws CUserNotFoundException {
		return findUserBySnsId(snsId).map(SignInResponseDto::new)
				.orElseThrow(() -> new CUserNotFoundException("Not found user matched to snsId: " + snsId));
	}
	
	public User findUser(Long id) throws CUserNotFoundException {
        return findUserById(id).orElseThrow(() -> new CUserNotFoundException("Not found user matched to id: " + id));
	}
	
	@Transactional
	public DeleteUserResponseDto delete(Long userId) throws CUserException {
	    return findUserById(userId).map(user -> {
	        user.getRooms().stream().forEach(room -> room.getUsers().remove(user));
	        user.getMemorys().stream().forEach(memory -> memory.getUsers().remove(user));
	        
	        delete(user);
	        return new DeleteUserResponseDto(currentDate());
	    })
	    .orElseThrow(() -> new CUserException("User Delete Failed: " + userId));
	}
	
	private Optional<User> insert(User user) {
	    return Optional.of(userRepo.save(user));
	}
	
	private Optional<User> findUserById(Long id) {
	    return userRepo.findById(id);
	}
	
	private Optional<User> findUserBySnsId(String snsId) {
	    return userRepo.findBySnsId(snsId);
	}
	
	private void delete(User user) {
	    userRepo.delete(user);
	}
}
