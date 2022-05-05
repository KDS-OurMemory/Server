package com.kds.ourmemory.room.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class RoomNotFoundOwnerException extends ArgsRuntimeException {

    public RoomNotFoundOwnerException(Object... args) {
        super(args);
    }
    
}
