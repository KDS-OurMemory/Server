package com.kds.ourmemory.room.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class RoomNotParticipantException extends ArgsRuntimeException {

    public RoomNotParticipantException(Object... args) {
        super(args);
    }

}
