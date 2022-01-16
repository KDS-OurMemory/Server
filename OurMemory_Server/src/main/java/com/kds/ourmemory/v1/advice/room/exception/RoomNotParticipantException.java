package com.kds.ourmemory.v1.advice.room.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class RoomNotParticipantException extends ArgsRuntimeException {

    public RoomNotParticipantException(Object... args) {
        super(args);
    }

}
