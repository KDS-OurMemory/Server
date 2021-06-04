package com.kds.ourmemory.entity.notice;

import com.kds.ourmemory.entity.BaseTimeEntity;
import com.kds.ourmemory.entity.user.User;
import com.kds.ourmemory.service.v1.notice.NoticeType;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

import java.io.Serializable;

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
    @Column(name = "notice_id")
    private Long id;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "user_id"))
    private User user;

    @Column(nullable = false, name = "notice_type")
    @Enumerated(EnumType.STRING)
    private NoticeType type;

    @Column(nullable = false, name = "notice_value")
    private String value;

    @Column(nullable = false, name = "notice_used")
    private Boolean used;

    @Builder
    public Notice(User user, NoticeType type, String value) {
        checkNotNull(user, "알림 대상 사용자 정보가 없습니다. 대상 사용자 번호를 확인해주세요.");
        checkNotNull(type, "알림 종류가 입력되지 않았습니다. 알림 종류를 입력해주세요.");
        checkArgument(StringUtils.isNotBlank(value), "알림 값이 입력되지 않았습니다. 알림 값을 입력해주세요.");

        this.user = user;
        this.type = type;
        this.value = value;
        this.used = true;
    }

    public void deleteNotice() {
        this.used = false;
    }

}
