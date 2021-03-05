package com.kds.ourmemory.repository.user;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kds.ourmemory.domain.user.Users;

@Transactional
public interface UserRepository extends JpaRepository<Users, Long> {
	public Optional<Users> findBySnsId(String snsId);
}
