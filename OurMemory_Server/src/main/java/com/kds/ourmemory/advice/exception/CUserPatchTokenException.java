package com.kds.ourmemory.advice.exception;

public class CUserPatchTokenException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CUserPatchTokenException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public CUserPatchTokenException(String msg) {
        super(msg);
    }
    
    public CUserPatchTokenException() {
        super();
    }
}
