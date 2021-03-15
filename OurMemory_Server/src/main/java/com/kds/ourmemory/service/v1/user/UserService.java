package com.kds.ourmemory.service.v1.user;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.advice.exception.CUserException;
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
		    DateFormat format = new SimpleDateFormat("yyyyMMdd");
	        String today = format.format(new Date());
	        
            firebaseFcm.sendMessageTo(user.getPushToken(), "OurMemory - SignUp", user.getName() + " is SignUp Success");
	        return new SignUpResponseDto(today);
		}).orElseThrow(() -> {
            firebaseFcm.sendMessageTo(user.getPushToken(), "OurMemory - SignUp", user.getName() + " is SignUp Failed.");
		    
		    throw new CUserException("signUP Failed.");
		});
	}

	public SignInResponseDto signIn(String snsId) throws CUserNotFoundException {
		return userRepo.findBySnsId(snsId).map(SignInResponseDto::new)
				.orElseThrow(() -> new CUserNotFoundException("No match snsId to user."));
	}
	
	private Optional<User> insert(User user) {
	    return Optional.of(userRepo.save(user));
	}
}
