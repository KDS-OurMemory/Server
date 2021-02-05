package com.kds.ourmemory.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Users {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")	// JPARepository ���� ������ũ ǥ����� �������� �ʾ� ī��� ����
	private Long id;
	
	@Column(nullable = false, name="user_sns_id")
	private String snsId;
	
	@Column(nullable = false, name="user_sns_type")	// 1: īī��, 2: ����, 3: ���̹�
	private int snsType;
	
	@Column(nullable = true, name="user_push_token")
	private String pushToken;
	
	@Column(nullable = true, name="user_name")
	private String name;
	
	@Column(nullable = true, name="user_birthday")
	private String birthday;
	
	@Column(nullable = false, name="user_solar_flag")
	private boolean isSolar;
	
	@Column(nullable = false, name="user_birthday_open_flag")
	private boolean isBirthdayOpen;
	
	@Column(nullable = true, name="user_role")
	private String role;
	
	@Column(nullable = false, name="reg_date")
	private Date regDate;
	
	@Column(nullable = false, name="user_used_flag")
	private boolean used;
}
