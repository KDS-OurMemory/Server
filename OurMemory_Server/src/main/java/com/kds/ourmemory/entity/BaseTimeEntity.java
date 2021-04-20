package com.kds.ourmemory.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @CreatedDate
    @Column(name = "reg_date", updatable = false)
    private LocalDateTime regDate;

    @LastModifiedDate
    @Column(name = "mod_date", nullable = true)
    private LocalDateTime modDate;
    
    public LocalDateTime getRegDate() {
        return Objects.nonNull(regDate)? LocalDateTime.parse(regDate.format(format), format) : null;
    }
    
    public LocalDateTime getModDate() {
        return Objects.nonNull(modDate)? LocalDateTime.parse(modDate.format(format), format) : null;
    }
    
    public String formatRegDate() {
        return Objects.nonNull(regDate)? regDate.format(format): null;
    }
    
    public String formatModDate() {
        return Objects.nonNull(modDate)? modDate.format(format): null;
    }
    
    public static String formatNow() {
        return LocalDateTime.now().format(format);
    }
}