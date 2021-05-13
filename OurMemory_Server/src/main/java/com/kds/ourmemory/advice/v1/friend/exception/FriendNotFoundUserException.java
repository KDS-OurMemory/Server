package com.kds.ourmemory.advice.v1.friend.exception;

import net.bytebuddy.implementation.bytecode.Throw;

public class FriendNotFoundUserException extends RuntimeException{
    public FriendNotFoundUserException(String msg, Throwable t) {
        super(msg, t);
    }

    public FriendNotFoundUserException(String msg) {
        super(msg);
    }

    public FriendNotFoundUserException() {
        super();
    }
}
