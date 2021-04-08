package com.kds.ourmemory.advice.v1.user.exception;

public class UserTokenUpdateException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UserTokenUpdateException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public UserTokenUpdateException(String msg) {
        super(msg);
    }
    
    public UserTokenUpdateException() {
        super();
    }
}
