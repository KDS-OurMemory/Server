package com.kds.ourmemory.friend.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class FriendInternalServerException extends ArgsRuntimeException {
    public FriendInternalServerException(Object... args) {
        super(args);
    }
}
