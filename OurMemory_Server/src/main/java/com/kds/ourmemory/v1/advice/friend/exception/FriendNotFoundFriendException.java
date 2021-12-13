package com.kds.ourmemory.v1.advice.friend.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class FriendNotFoundFriendException extends ArgsRuntimeException {
    public FriendNotFoundFriendException(Object... args) {
        super(args);
    }
}
