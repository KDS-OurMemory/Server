package com.kds.ourmemory.v1.repository.user;

import com.kds.ourmemory.v1.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySnsTypeAndSnsIdAndUsed(int snsType, String snsId, boolean used);

    @Query("select u from users u where u.used = ?1 and (u.id = ?2 or u.name = ?3)")
    Optional<List<User>> findAllByUsedAndIdOrName(boolean used, Long userId, String name);
}
