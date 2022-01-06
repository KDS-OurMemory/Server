package com.kds.ourmemory.v1.advice.friend.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class FriendNotFoundException extends ArgsRuntimeException {
    public FriendNotFoundException(Object... args) {
        super(args);
    }
}
