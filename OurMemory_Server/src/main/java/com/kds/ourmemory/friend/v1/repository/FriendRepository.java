package com.kds.ourmemory.friend.v1.repository;

import com.kds.ourmemory.friend.v1.entity.Friend;
import com.kds.ourmemory.friend.v1.entity.FriendId;
import com.kds.ourmemory.user.v1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public interface FriendRepository extends JpaRepository<Friend, FriendId> {
    Optional<List<Friend>> findAllByUserId(Long userId);
    Optional<Friend> findByUserIdAndFriendUserId(Long userId, Long friendId);
    Optional<List<Friend>> findAllByUserOrFriendUser(User user, User friendUser);
}
