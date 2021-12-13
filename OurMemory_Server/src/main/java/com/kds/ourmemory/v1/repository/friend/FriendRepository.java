package com.kds.ourmemory.v1.repository.friend;

import com.kds.ourmemory.v1.entity.friend.Friend;
import com.kds.ourmemory.v1.entity.friend.FriendId;
import com.kds.ourmemory.v1.entity.user.User;
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
