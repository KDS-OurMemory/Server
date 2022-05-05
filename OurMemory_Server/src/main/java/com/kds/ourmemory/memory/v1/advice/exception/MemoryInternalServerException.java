package com.kds.ourmemory.memory.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class MemoryInternalServerException extends ArgsRuntimeException {

    public MemoryInternalServerException(Object... args) {
        super(args);
    }
    
}
