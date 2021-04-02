package com.kds.ourmemory.advice.v1.room.exception;

public class RoomAddMemberException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RoomAddMemberException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public RoomAddMemberException(String msg) {
        super(msg);
    }
    
    public RoomAddMemberException() {
        super();
    }
}
