package com.kds.ourmemory.entity.friend;

import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.user.User;
import lombok.*;

import javax.persistence.*;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@ToString
@Entity(name = "friends")
@IdClass(FriendId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend extends BaseTimeEntity {

	@Id
	@ManyToOne(targetEntity = User.class)
	@JoinColumn(name = "user_id",
			foreignKey = @ForeignKey(name = "friends_user_id"))
	private User user;

	@Id
	@ManyToOne(targetEntity = User.class)
	@JoinColumn(name = "friend_id", foreignKey = @ForeignKey(name = "friends_friend_id"))
	private User friend;

	@Column(nullable = false, name = "friend_status")
	@Enumerated(EnumType.STRING)
	private FriendStatus status;

	public Friend(User user, User friend, FriendStatus status) {
		checkNotNull(user, "사용자 정보가 없습니다. 사용자 번호를 확인해주세요.");
		checkNotNull(friend, "친구 정보가 없습니다. 친구 번호를 확인해주세요.");

		this.user = user;
		this.friend = friend;
		this.status = status;
	}

	public Optional<Friend> changeStatus(FriendStatus status) {
		checkNotNull(status, "친구 상태값이 없습니다. 상태값을 입력해주시기 바랍니다.");

		return Optional.of(status)
				.flatMap(s -> {
					this.status = s;
					return Optional.of(this);
				});
	}
}
