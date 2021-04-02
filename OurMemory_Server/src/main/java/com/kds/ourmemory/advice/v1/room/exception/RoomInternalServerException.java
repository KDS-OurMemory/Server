package com.kds.ourmemory.advice.v1.room.exception;

public class RoomInternalServerException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RoomInternalServerException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public RoomInternalServerException(String msg) {
        super(msg);
    }
    
    public RoomInternalServerException() {
        super();
    }
}
