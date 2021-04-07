package com.kds.ourmemory.entity.userlog;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.kds.ourmemory.entity.user.User;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "userlogs")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "userlog_id")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User users;
	
	@Column(nullable = false, name="login_date")
	private Date loginDate;
}
