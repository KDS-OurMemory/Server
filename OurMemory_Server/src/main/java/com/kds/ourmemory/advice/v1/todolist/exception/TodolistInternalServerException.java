package com.kds.ourmemory.advice.v1.todolist.exception;

import java.io.Serial;

public class TodolistInternalServerException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public TodolistInternalServerException(String msg) {
        super(msg);
    }
}
