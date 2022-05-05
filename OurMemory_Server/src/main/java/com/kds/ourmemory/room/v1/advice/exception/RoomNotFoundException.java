package com.kds.ourmemory.room.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class RoomNotFoundException extends ArgsRuntimeException {

    public RoomNotFoundException(Object... args) {
        super(args);
    }

}
