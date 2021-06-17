package com.kds.ourmemory.advice.v1.memory.exception;

public class MemoryInternalServerException extends RuntimeException {
    public MemoryInternalServerException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public MemoryInternalServerException(String msg) {
        super(msg);
    }
    
    public MemoryInternalServerException() {
        super();
    }
}
