package com.kds.ourmemory.room.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class RoomInternalServerException extends ArgsRuntimeException {

    public RoomInternalServerException(Object... args) {
        super(args);
    }

}
