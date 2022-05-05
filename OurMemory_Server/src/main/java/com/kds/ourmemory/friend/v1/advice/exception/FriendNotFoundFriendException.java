package com.kds.ourmemory.friend.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class FriendNotFoundFriendException extends ArgsRuntimeException {
    public FriendNotFoundFriendException(Object... args) {
        super(args);
    }
}
