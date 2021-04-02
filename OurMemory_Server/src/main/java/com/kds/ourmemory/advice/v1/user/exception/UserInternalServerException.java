package com.kds.ourmemory.advice.v1.user.exception;

public class UserInternalServerException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UserInternalServerException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public UserInternalServerException(String msg) {
        super(msg);
    }
    
    public UserInternalServerException() {
        super();
    }
}
