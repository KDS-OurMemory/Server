package com.kds.ourmemory.memory.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class MemoryNotFoundShareMemberException extends ArgsRuntimeException {

    public MemoryNotFoundShareMemberException(Object... args) {
        super(args);
    }

}
