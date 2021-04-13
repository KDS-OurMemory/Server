package com.kds.ourmemory.repository.user;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.entity.user.User;

@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    
    public default Optional<User> insertUser(User user) throws UserInternalServerException {
        return Optional.ofNullable(this.save(user))
                .map(Optional::of)
                .orElseThrow(() -> new UserInternalServerException (
                        String.format("User '%s' insert failed.", user.getName())));
    }
    
    public default Optional<User> findUser(Long id) throws UserNotFoundException {
        return this.findById(id)
                .map(Optional::of)
                .orElseThrow(() -> new UserNotFoundException("Not found user matched to userId: " + id));
    }

    public Optional<User> findBySnsIdAndSnsType(String snsId, int snsType);
    public default Optional<User> findUser(String snsId, int snsType) throws UserNotFoundException {
        return this.findBySnsIdAndSnsType(snsId, snsType)
                .map(Optional::of)
                .orElseThrow(() -> new UserNotFoundException(String.format("Not found user matched to snsId: %s, snsType: %d", snsId, snsType)));
    }
}
