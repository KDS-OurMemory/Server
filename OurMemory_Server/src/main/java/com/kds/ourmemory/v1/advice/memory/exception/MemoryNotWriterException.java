package com.kds.ourmemory.v1.advice.memory.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class MemoryNotWriterException extends ArgsRuntimeException {

    public MemoryNotWriterException(Object... args) {
        super(args);
    }

}
