package com.kds.ourmemory.room.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class RoomAlreadyOwnerException extends ArgsRuntimeException {

    public RoomAlreadyOwnerException(Object... args) {
        super(args);
    }

}
