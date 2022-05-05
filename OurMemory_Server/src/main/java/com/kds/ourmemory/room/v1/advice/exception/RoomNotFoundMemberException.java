package com.kds.ourmemory.room.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class RoomNotFoundMemberException extends ArgsRuntimeException {

    public RoomNotFoundMemberException(Object... args) {
        super(args);
    }
    
}
