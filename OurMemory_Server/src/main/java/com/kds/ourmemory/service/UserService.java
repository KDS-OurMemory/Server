package com.kds.ourmemory.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.IntFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kds.ourmemory.domain.Users;
import com.kds.ourmemory.dto.signup.SignUpResponse;
import com.kds.ourmemory.repository.UserRepository;

@Service
public class UserService {

	private UserRepository repository;

	@Autowired
	public UserService(UserRepository repository) {
		this.repository = repository;
	}

	public SignUpResponse signUp(Users user) {
		Users saveUser = repository.save(user);

		DateFormat format = new SimpleDateFormat("yyyyMMdd");
		String today = format.format(new Date());

		IntFunction<SignUpResponse> response = code -> new SignUpResponse(code, today);
		return Optional.ofNullable(saveUser)
				.map(s -> response.apply(0))
				.orElseGet(() -> response.apply(1));
	}

	public Users signIn(String snsId) {
		return repository.findBySnsId(snsId)
				.orElse(new Users(null, "No Data", -1, "아이디에 맞는 사용자 없음.", null, null, false, false, null, null, false));
	}
}
