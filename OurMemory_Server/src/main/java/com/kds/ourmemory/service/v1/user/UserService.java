package com.kds.ourmemory.service.v1.user;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.advice.exception.CUsersException;
import com.kds.ourmemory.controller.v1.user.dto.SignUpResponseDto;
import com.kds.ourmemory.entity.user.Users;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FirebaseCloudMessageService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepo;
	private final FirebaseCloudMessageService firebaseFcm;

	public SignUpResponseDto signUp(Users user) {
		return insert(user).map(u -> {
		    DateFormat format = new SimpleDateFormat("yyyyMMdd");
	        String today = format.format(new Date());
	        
            firebaseFcm.sendMessageTo(user.getPushToken(), "OurMemory - SignUp", user.getName() + " is SignUp Success");
	        return new SignUpResponseDto(today);
		}).orElseThrow(() -> {
            firebaseFcm.sendMessageTo(user.getPushToken(), "OurMemory - SignUp", user.getName() + " is SignUp Failed.");
		    
		    throw new CUsersException();
		});
	}

	public Users signIn(String snsId) throws CUserNotFoundException {
		return userRepo.findBySnsId(snsId)
				.orElseThrow(() -> new CUserNotFoundException("No match snsId to user"));
	}
	
	private Optional<Users> insert(Users user) {
	    return Optional.of(userRepo.save(user));
	}
}
