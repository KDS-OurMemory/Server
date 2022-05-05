package com.kds.ourmemory.room.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class RoomNotOwnerException extends ArgsRuntimeException {

    public RoomNotOwnerException(Object... args) {
        super(args);
    }

}
