package com.kds.ourmemory.repository.user;

import com.kds.ourmemory.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySnsIdAndSnsType(String snsId, int snsType);

    @Query("select u from users u where u.used = ?1 and (u.id = ?2 or u.name = ?3)")
    Optional<List<User>> findAllByUsedAndIdOrName(boolean used, Long userId, String name);
}
