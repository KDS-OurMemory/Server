package com.kds.ourmemory.v1.advice.memory.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class MemoryNotFoundException extends ArgsRuntimeException {

    public MemoryNotFoundException(Object... args) {
        super(args);
    }
    
}
