package com.kds.ourmemory.friend.v1.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class FriendId implements Serializable {
    private Long user;
    private Long friendUser;
}
