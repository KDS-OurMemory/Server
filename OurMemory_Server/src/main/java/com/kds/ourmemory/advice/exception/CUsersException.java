package com.kds.ourmemory.advice.exception;

public class CUsersException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CUsersException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public CUsersException(String msg) {
        super(msg);
    }
    
    public CUsersException() {
        super();
    }
}
