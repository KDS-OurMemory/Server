package com.kds.ourmemory.advice.v1.todo.exception;

import java.io.Serial;

public class TodoNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public TodoNotFoundException(String msg) {
        super(msg);
    }
}
