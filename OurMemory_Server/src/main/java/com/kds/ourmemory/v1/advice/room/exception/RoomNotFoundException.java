package com.kds.ourmemory.v1.advice.room.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class RoomNotFoundException extends ArgsRuntimeException {

    public RoomNotFoundException(Object... args) {
        super(args);
    }

}
