package com.kds.ourmemory.user.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class UserNotSignUpException extends ArgsRuntimeException {

    public UserNotSignUpException(Object... args) {
        super(args);
    }

}
