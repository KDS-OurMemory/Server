package com.kds.ourmemory.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kds.ourmemory.domain.Users;
import com.kds.ourmemory.dto.signup.SignUpRequest;
import com.kds.ourmemory.dto.signup.SignUpResponse;
import com.kds.ourmemory.service.UserService;

import io.swagger.annotations.ApiOperation;

@RestController(value = "/User")
public class UserController {
	private UserService service;
	
	@Autowired
	public UserController(UserService service) {
		this.service= service;
	}

	@ApiOperation(value="회원가입", notes = "앱에서 전달받은 데이터로 회원가입 진행.")
	@PostMapping("/SignUp")
	public SignUpResponse signUp(@RequestBody SignUpRequest request) {
		System.out.println(request.getSnsId());
		Users user = new Users(null, request.getSnsId(), request.getSnsType(), request.getPushToken(),
				request.getName(), request.getBirthday(), request.isSolar(), request.isBirthdayOpen(), "user",
				new Date(), true);
		
		return service.signUp(user);
	}
	
	@ApiOperation(value="로그인", notes = "userId 로 사용자 정보 조회 및 리턴")
	@GetMapping("/SignIn")
	public Users signIn(@RequestParam String snsId) {
		return service.signIn(snsId);
	}
}