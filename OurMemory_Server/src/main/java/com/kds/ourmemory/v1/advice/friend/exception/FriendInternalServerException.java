package com.kds.ourmemory.v1.advice.friend.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class FriendInternalServerException extends ArgsRuntimeException {
    public FriendInternalServerException(Object... args) {
        super(args);
    }
}
