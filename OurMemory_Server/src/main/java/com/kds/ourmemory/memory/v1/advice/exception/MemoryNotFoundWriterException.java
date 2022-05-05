package com.kds.ourmemory.memory.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class MemoryNotFoundWriterException extends ArgsRuntimeException {

    public MemoryNotFoundWriterException(Object... args) {
        super(args);
    }
    
}
