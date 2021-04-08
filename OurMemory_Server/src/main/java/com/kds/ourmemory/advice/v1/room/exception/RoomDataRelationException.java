package com.kds.ourmemory.advice.v1.room.exception;

public class RoomDataRelationException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RoomDataRelationException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public RoomDataRelationException(String msg) {
        super(msg);
    }
    
    public RoomDataRelationException() {
        super();
    }
}
