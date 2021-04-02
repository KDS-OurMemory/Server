package com.kds.ourmemory.advice.v1.room.exception;

public class RoomNotFoundOwnerException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RoomNotFoundOwnerException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public RoomNotFoundOwnerException(String msg) {
        super(msg);
    }
    
    public RoomNotFoundOwnerException() {
        super();
    }
}
