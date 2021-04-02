package com.kds.ourmemory.advice.v1.user.exception;

public class UserInterServerException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UserInterServerException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public UserInterServerException(String msg) {
        super(msg);
    }
    
    public UserInterServerException() {
        super();
    }
}
