package com.kds.ourmemory.dto.signup;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {
	private String snsId;
	private int snsType;	// 1: īī��, 2: ����, 3: ���̹�
	private String pushToken;
	private String name;
	private String birthday;
	private boolean isSolar;
	private boolean isBirthdayOpen;
}
