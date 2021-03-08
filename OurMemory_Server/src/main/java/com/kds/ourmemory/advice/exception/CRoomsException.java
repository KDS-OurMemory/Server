package com.kds.ourmemory.advice.exception;

public class CRoomsException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CRoomsException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public CRoomsException(String msg) {
        super(msg);
    }
    
    public CRoomsException() {
        super();
    }
}
