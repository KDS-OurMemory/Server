package com.kds.ourmemory.repository.friend;

import com.kds.ourmemory.entity.friend.Friend;
import com.kds.ourmemory.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface FriendRepository extends JpaRepository<Friend, User> {
    public Optional<List<Friend>> FindByUserId(Long userId);
}
