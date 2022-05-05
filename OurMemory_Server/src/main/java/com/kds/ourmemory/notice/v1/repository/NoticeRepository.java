package com.kds.ourmemory.notice.v1.repository;

import com.kds.ourmemory.notice.v1.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Optional<List<Notice>> findAllByUserId(Long userId);
}
