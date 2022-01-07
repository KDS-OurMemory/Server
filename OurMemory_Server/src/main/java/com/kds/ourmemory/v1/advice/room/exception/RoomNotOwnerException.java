package com.kds.ourmemory.v1.advice.room.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class RoomNotOwnerException extends ArgsRuntimeException {

    public RoomNotOwnerException(Object... args) {
        super(args);
    }

}
