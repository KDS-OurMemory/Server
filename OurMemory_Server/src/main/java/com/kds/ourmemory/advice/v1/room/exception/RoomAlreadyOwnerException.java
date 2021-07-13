package com.kds.ourmemory.advice.v1.room.exception;

public class RoomAlreadyOwnerException extends RuntimeException {

    public RoomAlreadyOwnerException(String msg) {
        super(msg);
    }
}
