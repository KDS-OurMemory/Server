package com.kds.ourmemory.memory.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class MemoryNotIncludeRoomException extends ArgsRuntimeException {

    public MemoryNotIncludeRoomException(Object... args) {
        super(args);
    }

}
