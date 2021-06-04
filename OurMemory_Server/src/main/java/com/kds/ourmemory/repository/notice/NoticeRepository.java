package com.kds.ourmemory.repository.notice;

import com.kds.ourmemory.entity.notice.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Optional<List<Notice>> findAllByUserId(Long userId);
}
