package com.kds.ourmemory.advice.v1.todo.exception;

import java.io.Serial;

public class TodoInternalServerException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public TodoInternalServerException(String msg) {
        super(msg);
    }
}
