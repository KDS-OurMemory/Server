package com.kds.ourmemory.service.v1.user;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kds.ourmemory.advice.exception.CNotFoundUserException;
import com.kds.ourmemory.domain.Users;
import com.kds.ourmemory.dto.signup.SignUpResponse;
import com.kds.ourmemory.repository.UserRepository;
import com.kds.ourmemory.service.v1.firebase.FirebaseCloudMessageService;

@Service
public class UserService {

	private UserRepository repository;
	private FirebaseCloudMessageService firebaseFcm;

	@Autowired
	public UserService(UserRepository repository, FirebaseCloudMessageService firebaseFcm) {
		this.repository = repository;
		this.firebaseFcm = firebaseFcm;
	}

	public SignUpResponse signUp(Users user) {
		Users saveUser = repository.save(user);

		DateFormat format = new SimpleDateFormat("yyyyMMdd");
		String today = format.format(new Date());

		IntFunction<SignUpResponse> response = code -> new SignUpResponse(code, today);
		
		Consumer<String> fcmPush = result -> firebaseFcm.sendMessageTo(user.getPushToken(), 
																		"OurMemory - SignUp", 
																		user.getName() + " is SignUp " + result);
		Optional.ofNullable(saveUser)
			.ifPresentOrElse(u -> fcmPush.accept("Success"), () -> fcmPush.accept("Fail"));
		
		return Optional.ofNullable(saveUser)
				.map(s -> response.apply(0))
				.orElseGet(() -> response.apply(1));
	}

	public Users signIn(String snsId) throws CNotFoundUserException {
		return repository.findBySnsId(snsId)
				.orElseThrow(() -> new CNotFoundUserException("No match snsId to user"));
	}
}
