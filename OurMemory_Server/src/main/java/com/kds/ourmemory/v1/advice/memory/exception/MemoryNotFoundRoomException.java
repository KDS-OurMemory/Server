package com.kds.ourmemory.v1.advice.memory.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class MemoryNotFoundRoomException extends ArgsRuntimeException {

    public MemoryNotFoundRoomException(Object... args) {
        super(args);
    }
    
}
