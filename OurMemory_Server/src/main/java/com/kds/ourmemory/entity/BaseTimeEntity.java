package com.kds.ourmemory.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
    
    @Embedded
    @CreatedDate
    @Column(name = "reg_date")
    private CLocalDateTime regDate;

    @Embedded
    @LastModifiedDate
    @Column(name = "mod_date", nullable = true)
    private CLocalDateTime modDate;
    
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CLocalDateTime {
        private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        private LocalDateTime time;
        
        public LocalDateTime getTime() {
            return LocalDateTime.parse(time.format(format), format);
        }
        
        public static CLocalDateTime formatTime(LocalDateTime d) {
            CLocalDateTime cLocalDateTime = new CLocalDateTime();
            cLocalDateTime.time = LocalDateTime.parse(d.format(format), format);
            return cLocalDateTime;
        }
        
        public static CLocalDateTime formatTime(String dStr) {
            CLocalDateTime cLocalDateTime = new CLocalDateTime();
            cLocalDateTime.time = LocalDateTime.parse(dStr, format);
            return cLocalDateTime;
        }
    }
}
