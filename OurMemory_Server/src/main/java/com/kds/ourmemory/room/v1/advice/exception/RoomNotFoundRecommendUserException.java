package com.kds.ourmemory.room.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class RoomNotFoundRecommendUserException extends ArgsRuntimeException {

    public RoomNotFoundRecommendUserException(Object... args) {
        super(args);
    }

}
