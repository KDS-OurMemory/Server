package com.kds.ourmemory.advice.v1.memory.exception;

public class MemoryNotFoundException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MemoryNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public MemoryNotFoundException(String msg) {
        super(msg);
    }
    
    public MemoryNotFoundException() {
        super();
    }
}
