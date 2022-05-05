package com.kds.ourmemory.user.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class UserInternalServerException extends ArgsRuntimeException {

    public UserInternalServerException(Object... args) {
        super(args);
    }
    
}
