package com.kds.ourmemory.v1.advice.room.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class RoomNotFoundOwnerException extends ArgsRuntimeException {

    public RoomNotFoundOwnerException(Object... args) {
        super(args);
    }
    
}
