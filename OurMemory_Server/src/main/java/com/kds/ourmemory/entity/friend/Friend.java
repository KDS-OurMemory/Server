package com.kds.ourmemory.entity.friend;

import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.user.User;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@ToString
@Entity(name = "friends")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend extends BaseTimeEntity {

	@ManyToOne(targetEntity = User.class)
	@JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "friends_user_id"))
	private User user;

	@ManyToOne(targetEntity = User.class)
	@JoinColumn(name = "friend_id", foreignKey = @ForeignKey(name = "friends_friend_id"))
	private User friend;
}
