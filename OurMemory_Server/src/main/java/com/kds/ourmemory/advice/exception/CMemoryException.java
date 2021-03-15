package com.kds.ourmemory.advice.exception;

public class CMemoryException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CMemoryException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public CMemoryException(String msg) {
        super(msg);
    }
    
    public CMemoryException() {
        super();
    }
}
