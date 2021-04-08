package com.kds.ourmemory.advice.v1.room.exception;

public class RoomNotFoundException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RoomNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public RoomNotFoundException(String msg) {
        super(msg);
    }
    
    public RoomNotFoundException() {
        super();
    }
}
