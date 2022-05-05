package com.kds.ourmemory.relation.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class UserMemoryInternalServerException extends ArgsRuntimeException {
    public UserMemoryInternalServerException(Object... args) {
        super(args);
    }
}
