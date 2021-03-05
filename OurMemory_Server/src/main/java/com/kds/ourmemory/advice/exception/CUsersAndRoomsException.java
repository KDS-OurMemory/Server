package com.kds.ourmemory.advice.exception;

public class CUsersAndRoomsException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CUsersAndRoomsException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public CUsersAndRoomsException(String msg) {
        super(msg);
    }
    
    public CUsersAndRoomsException() {
        super();
    }
}
