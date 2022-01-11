package com.kds.ourmemory.v1.advice.room.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class RoomNotFoundRecommendUserException extends ArgsRuntimeException {

    public RoomNotFoundRecommendUserException(Object... args) {
        super(args);
    }

}
