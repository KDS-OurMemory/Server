package com.kds.ourmemory.advice.v1.room.exception;

public class RoomNotFoundMemberException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RoomNotFoundMemberException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public RoomNotFoundMemberException(String msg) {
        super(msg);
    }
    
    public RoomNotFoundMemberException() {
        super();
    }
}
