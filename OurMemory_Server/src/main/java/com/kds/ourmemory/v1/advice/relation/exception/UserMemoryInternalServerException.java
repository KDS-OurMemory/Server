package com.kds.ourmemory.v1.advice.relation.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class UserMemoryInternalServerException extends ArgsRuntimeException {
    public UserMemoryInternalServerException(Object... args) {
        super(args);
    }
}
