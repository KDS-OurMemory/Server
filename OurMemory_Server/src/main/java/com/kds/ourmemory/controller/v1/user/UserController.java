package com.kds.ourmemory.controller.v1.user;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kds.ourmemory.advice.exception.CNotFoundUserException;
import com.kds.ourmemory.domain.Users;
import com.kds.ourmemory.dto.user.SignUpRequestDto;
import com.kds.ourmemory.dto.user.SignUpResponseDto;
import com.kds.ourmemory.service.v1.user.UserService;

import io.swagger.annotations.ApiOperation;

@RestController(value = "/v1")
public class UserController {
	private UserService service;
	
	@Autowired
	public UserController(UserService service) {
		this.service= service;
	}

	@ApiOperation(value="회원가입", notes = "앱에서 전달받은 데이터로 회원가입 진행")
	@PostMapping("/signUp")
	public SignUpResponseDto signUp(@RequestBody SignUpRequestDto request) {
		Users user = Users.builder()
				.id(null)
				.snsId(request.getSnsId())
				.snsType(request.getSnsType())
				.pushToken(request.getPushToken())
				.name(request.getName())
				.birthday(request.getBirthday())
				.isSolar(request.isSolar())
				.isBirthdayOpen(request.isBirthdayOpen())
				.role("user")
				.regDate(new Date())
				.used(true)
				.build();
		
		return service.signUp(user);
	}
	
	@ApiOperation(value="로그인", notes = "snsId 로 사용저 정보 조회 및 리턴")
	@GetMapping("/signIn")
	public Users signIn(@RequestParam String snsId) throws CNotFoundUserException {
		return service.signIn(snsId);
	}
}