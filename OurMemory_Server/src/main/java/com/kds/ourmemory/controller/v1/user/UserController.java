package com.kds.ourmemory.controller.v1.user;

import java.util.Date;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.controller.v1.user.dto.SignUpRequestDto;
import com.kds.ourmemory.controller.v1.user.dto.SignUpResponseDto;
import com.kds.ourmemory.entity.user.Users;
import com.kds.ourmemory.service.v1.user.UserService;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class UserController {
    
	private final UserService service;
	
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
	public Users signIn(@RequestParam String snsId) throws CUserNotFoundException {
		return service.signIn(snsId);
	}
}