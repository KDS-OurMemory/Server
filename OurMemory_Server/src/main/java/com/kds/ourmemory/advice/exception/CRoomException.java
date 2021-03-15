package com.kds.ourmemory.advice.exception;

public class CRoomException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CRoomException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public CRoomException(String msg) {
        super(msg);
    }
    
    public CRoomException() {
        super();
    }
}
