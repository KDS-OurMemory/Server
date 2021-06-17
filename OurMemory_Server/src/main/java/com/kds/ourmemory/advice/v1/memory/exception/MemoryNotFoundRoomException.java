package com.kds.ourmemory.advice.v1.memory.exception;

public class MemoryNotFoundRoomException extends RuntimeException {
    public MemoryNotFoundRoomException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public MemoryNotFoundRoomException(String msg) {
        super(msg);
    }
    
    public MemoryNotFoundRoomException() {
        super();
    }
}
