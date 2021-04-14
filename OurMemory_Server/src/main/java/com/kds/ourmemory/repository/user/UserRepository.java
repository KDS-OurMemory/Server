package com.kds.ourmemory.repository.user;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kds.ourmemory.entity.user.User;

@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findBySnsIdAndSnsType(String snsId, int snsType);
}
