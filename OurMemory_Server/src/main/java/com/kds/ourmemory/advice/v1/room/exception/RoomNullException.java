package com.kds.ourmemory.advice.v1.room.exception;

public class RoomNullException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RoomNullException(String msg, Throwable t) {
        super(msg, t);
    }

    public RoomNullException(String msg) {
        super(msg);
    }

    public RoomNullException() {
        super();
    }
}
