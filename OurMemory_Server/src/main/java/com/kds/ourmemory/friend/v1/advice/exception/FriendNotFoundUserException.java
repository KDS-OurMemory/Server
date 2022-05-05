package com.kds.ourmemory.friend.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class FriendNotFoundUserException extends ArgsRuntimeException {

    public FriendNotFoundUserException(Object... args) {
        super(args);
    }

}
