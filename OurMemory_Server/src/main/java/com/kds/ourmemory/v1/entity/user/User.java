package com.kds.ourmemory.v1.entity.user;

import com.kds.ourmemory.v1.controller.user.dto.UserReqDto;
import com.kds.ourmemory.v1.encrypt.ColumnEncryptConverter;
import com.kds.ourmemory.v1.entity.BaseTimeEntity;
import com.kds.ourmemory.v1.entity.relation.UserMemory;
import com.kds.ourmemory.v1.entity.room.Room;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


@DynamicUpdate
@Entity(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")	// JPARepository 에서 스네이크 표기법을 지원하지 않아 카멜로 수정
	private Long id;

    @Comment("1: 카카오, 2: 구글, 3: 네이버")
    @Column(nullable = false, name="user_sns_type")
    private int snsType;
    
	@Column(nullable = false, name="user_sns_id")
	private String snsId;
	
	@Column(name="user_push_token")
	private String pushToken;

	@Column(nullable = false, name="user_fcm_push_flag")
    private boolean push;

    @Convert(converter = ColumnEncryptConverter.class)
	@Column(name="user_name")
	private String name;
	
	@Column(name="user_birthday")
	private String birthday;

	@Column(nullable = false, name="user_solar_flag")
	private boolean solar;

	@Column(nullable = false, name="user_birthday_open_flag")
	private boolean birthdayOpen;

    @Comment("USER: 사용자, ADMIN: 관리자")
	@Column(nullable = false, name="user_role")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Comment("ANDROID: 안드로이드 OS, IOS: 아이폰 OS")
    @Column(nullable = false, name="user_device_os")
    @Enumerated(EnumType.STRING)
    private DeviceOs deviceOs;

    @Column(name="user_private_room_id")
    private Long privateRoomId;

    @Column(name="user_profile_image_url")
    private String profileImageUrl;

	@Column(nullable = false, name="user_used_flag")
	private boolean used;

	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="users_rooms",
                joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "room_id"))
	private List<Room> rooms = new ArrayList<>();

	@OneToMany(mappedBy = "user")
    private List<UserMemory> memories = new ArrayList<>();

	@Builder
    public User(Long id, int snsType, String snsId, String pushToken, Boolean push, String name, String birthday,
                boolean solar, boolean birthdayOpen, UserRole role, DeviceOs deviceOs) {
	    checkArgument(1 <= snsType && snsType <= 3, "지원하지 않는 SNS 인증방식입니다. 카카오(1), 구글(2), 네이버(3) 중에 입력해주시기 바랍니다.");
        checkArgument(StringUtils.isNoneBlank(snsId), "SNS ID 는 빈 값이 될 수 없습니다.");
        checkArgument(StringUtils.isNoneBlank(name), "이름이 입력되지 않았습니다. 이름을 입력해주세요.");
        checkNotNull(deviceOs, "기기 종류는 빈 값이 될 수 없습니다.");
        checkNotNull(push, "푸시 사용여부는 빈 값이 될 수 없습니다.");

        this.id = id;
        this.snsType = snsType;
        this.snsId = snsId;
        this.pushToken = pushToken;
        this.push = push;
        this.name = name;
        this.birthday = birthday;
        this.solar = solar;
        this.birthdayOpen = birthdayOpen;
        this.role = role;
        this.deviceOs = deviceOs;
        this.used = true;
    }
	
	public void addRoom(Room room) {
	    this.rooms = this.rooms==null? new ArrayList<>() : this.rooms;
	    this.rooms.add(room);
	}

	public void addMemory(UserMemory userMemory) {
	    this.memories.add(userMemory);
    }

    public void updatePrivateRoomId(Long privateRoomId) {
	    this.privateRoomId = privateRoomId;
    }

    public User updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        return this;
    }
    
    public Optional<User> changePushToken(String pushToken) {
        checkArgument(StringUtils.isNoneBlank(pushToken), "토큰 값은 빈 값이 될 수 없습니다.");

        return Optional.of(pushToken)
                .map(t -> {
                    this.pushToken = t;
                    return this;
                });
    }

    public Optional<User> updateUser(UserReqDto request) {
        checkNotNull(request, "업데이트할 값이 입력되지 않았습니다.");
        return Optional.of(request)
                .map(req -> {
                    name = Objects.nonNull(req.getName()) ? req.getName() : name;
                    birthday = Objects.nonNull(req.getBirthday()) ? req.getBirthday() : birthday;
                    solar = Objects.nonNull(req.getSolar()) ? req.getSolar() : solar;
                    birthdayOpen = Objects.nonNull(req.getBirthdayOpen()) ? req.getBirthdayOpen() : birthdayOpen;
                    push = Objects.nonNull(req.getPush()) ? req.getPush() : push;

                    return this;
                });
    }

    public void deleteUser() {
	    this.used = false;
    }

    public void deleteRooms(List<Room> rooms) {
	    this.rooms.removeAll(rooms);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;

        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return 562048007;
    }
}
