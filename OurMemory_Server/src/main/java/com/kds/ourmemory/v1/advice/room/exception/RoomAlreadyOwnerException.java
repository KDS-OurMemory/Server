package com.kds.ourmemory.v1.advice.room.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class RoomAlreadyOwnerException extends ArgsRuntimeException {

    public RoomAlreadyOwnerException(Object... args) {
        super(args);
    }

}
