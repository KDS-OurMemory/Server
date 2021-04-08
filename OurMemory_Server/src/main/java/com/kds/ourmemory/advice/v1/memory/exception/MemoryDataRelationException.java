package com.kds.ourmemory.advice.v1.memory.exception;

public class MemoryDataRelationException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    
    public MemoryDataRelationException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public MemoryDataRelationException(String msg) {
        super(msg);
    }
    
    public MemoryDataRelationException() {
        super();
    }
}
