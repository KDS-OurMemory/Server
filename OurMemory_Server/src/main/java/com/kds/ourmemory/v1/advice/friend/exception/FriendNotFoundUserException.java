package com.kds.ourmemory.v1.advice.friend.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class FriendNotFoundUserException extends ArgsRuntimeException {

    public FriendNotFoundUserException(Object... args) {
        super(args);
    }

}