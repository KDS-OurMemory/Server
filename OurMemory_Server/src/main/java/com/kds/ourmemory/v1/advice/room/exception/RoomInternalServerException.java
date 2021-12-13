package com.kds.ourmemory.v1.advice.room.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class RoomInternalServerException extends ArgsRuntimeException {

    public RoomInternalServerException(Object... args) {
        super(args);
    }

}
