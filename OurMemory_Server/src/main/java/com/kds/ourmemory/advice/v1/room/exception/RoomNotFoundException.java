package com.kds.ourmemory.advice.v1.room.exception;

import java.io.Serial;

public class RoomNotFoundException extends RuntimeException{
    /**
     * 
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public RoomNotFoundException(String msg) {
        super(msg);
    }
}
