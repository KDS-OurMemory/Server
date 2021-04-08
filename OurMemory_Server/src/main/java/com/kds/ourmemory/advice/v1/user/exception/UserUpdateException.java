package com.kds.ourmemory.advice.v1.user.exception;

public class UserUpdateException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UserUpdateException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public UserUpdateException(String msg) {
        super(msg);
    }
    
    public UserUpdateException() {
        super();
    }
}
