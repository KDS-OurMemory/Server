package com.kds.ourmemory.memory.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class MemoryNotWriterException extends ArgsRuntimeException {

    public MemoryNotWriterException(Object... args) {
        super(args);
    }

}
