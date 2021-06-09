package com.kds.ourmemory.entity.friend;

import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.user.User;
import lombok.*;

import javax.persistence.*;

@ToString
@Entity(name = "friends")
@IdClass(FriendId.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend extends BaseTimeEntity {

	@Id
	@ManyToOne(targetEntity = User.class)
	@JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "friends_user_id"))
	private User user;

	@Id
	@ManyToOne(targetEntity = User.class)
	@JoinColumn(name = "friend_id", foreignKey = @ForeignKey(name = "friends_friend_id"))
	private User friend;

	@Column(name = "friend_status")
	@Enumerated(EnumType.STRING)
	private FriendStatus status;
}
