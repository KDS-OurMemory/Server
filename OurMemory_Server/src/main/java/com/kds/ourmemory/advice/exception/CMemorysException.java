package com.kds.ourmemory.advice.exception;

public class CMemorysException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CMemorysException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public CMemorysException(String msg) {
        super(msg);
    }
    
    public CMemorysException() {
        super();
    }
}
