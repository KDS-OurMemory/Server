package com.kds.ourmemory.advice.v1.friend.exception;

public class FriendNotFoundException extends RuntimeException {
    public FriendNotFoundException(String msg) {
        super(msg);
    }
}
