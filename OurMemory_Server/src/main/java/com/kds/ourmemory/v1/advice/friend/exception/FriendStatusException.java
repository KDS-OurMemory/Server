package com.kds.ourmemory.v1.advice.friend.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class FriendStatusException extends ArgsRuntimeException {
    public FriendStatusException(Object... args) {
        super(args);
    }
}
