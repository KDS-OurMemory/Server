package com.kds.ourmemory.v1.advice.memory.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class MemoryNotFoundWriterException extends ArgsRuntimeException {

    public MemoryNotFoundWriterException(Object... args) {
        super(args);
    }
    
}
