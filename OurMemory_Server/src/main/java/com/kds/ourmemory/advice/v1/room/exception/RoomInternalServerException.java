package com.kds.ourmemory.advice.v1.room.exception;

import java.io.Serial;

public class RoomInternalServerException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1L;

    public RoomInternalServerException(String msg) {
        super(msg);
    }
}
