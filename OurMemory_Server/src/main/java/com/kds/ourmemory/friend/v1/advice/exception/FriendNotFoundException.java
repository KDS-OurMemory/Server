package com.kds.ourmemory.friend.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class FriendNotFoundException extends ArgsRuntimeException {
    public FriendNotFoundException(Object... args) {
        super(args);
    }
}
