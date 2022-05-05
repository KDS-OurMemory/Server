package com.kds.ourmemory.memory.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class MemoryNotFoundRoomException extends ArgsRuntimeException {

    public MemoryNotFoundRoomException(Object... args) {
        super(args);
    }
    
}
