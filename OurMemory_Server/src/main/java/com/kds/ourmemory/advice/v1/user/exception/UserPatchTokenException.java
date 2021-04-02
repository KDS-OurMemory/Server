package com.kds.ourmemory.advice.v1.user.exception;

public class UserPatchTokenException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UserPatchTokenException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public UserPatchTokenException(String msg) {
        super(msg);
    }
    
    public UserPatchTokenException() {
        super();
    }
}
