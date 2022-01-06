package com.kds.ourmemory.v1.advice.room.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class RoomNotFoundMemberException extends ArgsRuntimeException {

    public RoomNotFoundMemberException(Object... args) {
        super(args);
    }
    
}
