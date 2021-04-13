package com.kds.ourmemory.advice.v1.memory.exception;

public class MemoryBadParameterException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MemoryBadParameterException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public MemoryBadParameterException(String msg) {
        super(msg);
    }
    
    public MemoryBadParameterException() {
        super();
    }
}
