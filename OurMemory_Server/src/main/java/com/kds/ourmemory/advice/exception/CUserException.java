package com.kds.ourmemory.advice.exception;

public class CUserException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CUserException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public CUserException(String msg) {
        super(msg);
    }
    
    public CUserException() {
        super();
    }
}
