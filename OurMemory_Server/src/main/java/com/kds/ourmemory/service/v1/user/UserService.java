package com.kds.ourmemory.service.v1.user;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.controller.v1.user.dto.SignUpResponseDto;
import com.kds.ourmemory.entity.user.Users;
import com.kds.ourmemory.repository.user.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FirebaseCloudMessageService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository repository;
	private final FirebaseCloudMessageService firebaseFcm;

	public SignUpResponseDto signUp(Users user) {
		Users saveUser = repository.save(user);

		DateFormat format = new SimpleDateFormat("yyyyMMdd");
		String today = format.format(new Date());

		IntFunction<SignUpResponseDto> response = code -> new SignUpResponseDto(code, today);
		
		Consumer<String> fcmPush = result -> firebaseFcm.sendMessageTo(user.getPushToken(), 
																		"OurMemory - SignUp", 
																		user.getName() + " is SignUp " + result);
		Optional.ofNullable(saveUser)
			.ifPresentOrElse(u -> fcmPush.accept("Success"), () -> fcmPush.accept("Fail"));
		
		return Optional.ofNullable(saveUser)
				.map(s -> response.apply(0))
				.orElseGet(() -> response.apply(1));
	}

	public Users signIn(String snsId) throws CUserNotFoundException {
		return repository.findBySnsId(snsId)
				.orElseThrow(() -> new CUserNotFoundException("No match snsId to user"));
	}
}
