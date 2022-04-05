package com.kds.ourmemory.v1.entity.notice;

import com.kds.ourmemory.v1.entity.BaseTimeEntity;
import com.kds.ourmemory.v1.entity.user.User;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@ToString
@DynamicUpdate
@Entity(name = "notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id", nullable = false)
    private Long id;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "user_id"))
    private User user;

    @Comment("FRIEND_REQUEST: 친구 요청")
    @Column(nullable = false, name = "notice_type")
    @Enumerated(EnumType.STRING)
    private NoticeType type;

    @Column(nullable = false, name = "notice_value")
    private String value;

    @Column(nullable = false, name = "notice_read_flag")
    private boolean read;

    @Column(nullable = false, name = "notice_used_flag")
    private boolean used;

    @Builder
    public Notice(User user, NoticeType type, String value) {
        checkNotNull(user, "알림 대상 사용자 정보가 없습니다. 대상 사용자 번호를 확인해주세요.");
        checkNotNull(type, "알림 종류가 입력되지 않았습니다. 알림 종류를 입력해주세요.");
        checkArgument(StringUtils.isNotBlank(value), "알림 값이 입력되지 않았습니다. 알림 값을 입력해주세요.");

        this.user = user;
        this.type = type;
        this.value = value;
        this.read = false;
        this.used = true;
    }

    public void readNotice() { this.read = true; }

    public void deleteNotice() {
        this.used = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Notice notice = (Notice) o;

        return Objects.equals(id, notice.id);
    }

    @Override
    public int hashCode() {
        return 115781988;
    }
}
