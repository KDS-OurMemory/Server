package com.kds.ourmemory.advice.v1.friend.exception;

public class FriendAlreadyAcceptException extends RuntimeException {

    public FriendAlreadyAcceptException(String msg, Throwable t) {
        super(msg, t);
    }

    public FriendAlreadyAcceptException(String msg) {
        super(msg);
    }

    public FriendAlreadyAcceptException() {
        super();
    }
}
