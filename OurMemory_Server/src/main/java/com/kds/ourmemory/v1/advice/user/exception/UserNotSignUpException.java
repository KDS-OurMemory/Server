package com.kds.ourmemory.v1.advice.user.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class UserNotSignUpException extends ArgsRuntimeException {

    public UserNotSignUpException(Object... args) {
        super(args);
    }

}
