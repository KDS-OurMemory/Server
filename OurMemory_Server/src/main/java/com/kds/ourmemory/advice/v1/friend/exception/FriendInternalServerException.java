package com.kds.ourmemory.advice.v1.friend.exception;

public class FriendInternalServerException extends RuntimeException {
    public FriendInternalServerException(String msg, Throwable t) {
        super(msg, t);
    }

    public FriendInternalServerException(String msg) {
        super(msg);
    }

    public FriendInternalServerException() {
        super();
    }
}
