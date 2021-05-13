package com.kds.ourmemory.advice.v1.friend.exception;

public class FriendNotFoundFriendException extends RuntimeException {
    public FriendNotFoundFriendException(String msg, Throwable t) {
        super(msg, t);
    }

    public FriendNotFoundFriendException(String msg) {
        super(msg);
    }

    public FriendNotFoundFriendException() {
        super();
    }
}
