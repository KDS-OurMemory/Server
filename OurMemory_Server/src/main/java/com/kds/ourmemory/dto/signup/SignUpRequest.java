package com.kds.ourmemory.dto.signup;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {
	private String snsId;
	private int snsType;	// 1: 카카오, 2: 구글, 3: 네이버
	private String pushToken;
	private String name;
	private String birthday;
	private boolean isSolar;
	private boolean isBirthdayOpen;
}
