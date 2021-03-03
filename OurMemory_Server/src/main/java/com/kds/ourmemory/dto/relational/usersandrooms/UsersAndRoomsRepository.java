package com.kds.ourmemory.dto.relational.usersandrooms;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kds.ourmemory.domain.UsersAndRooms;
import com.kds.ourmemory.domain.UsersAndRoomsPk;

public interface UsersAndRoomsRepository extends JpaRepository<UsersAndRooms, UsersAndRoomsPk>{
}
