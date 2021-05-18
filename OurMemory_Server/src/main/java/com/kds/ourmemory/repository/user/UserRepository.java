package com.kds.ourmemory.repository.user;

import com.kds.ourmemory.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySnsIdAndSnsType(String snsId, int snsType);
    Optional<List<User>> findAllByUserIdOrName(Long userId, String name);
}
