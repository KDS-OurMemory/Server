package com.kds.ourmemory.v1.advice.user.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class UserInternalServerException extends ArgsRuntimeException {

    public UserInternalServerException(Object... args) {
        super(args);
    }
    
}
