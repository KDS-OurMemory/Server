package com.kds.ourmemory.advice.v1.memory.exception;

public class MemoryNotFoundWriterException extends RuntimeException{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MemoryNotFoundWriterException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public MemoryNotFoundWriterException(String msg) {
        super(msg);
    }
    
    public MemoryNotFoundWriterException() {
        super();
    }
}
