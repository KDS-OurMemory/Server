package com.kds.ourmemory.advice.v1.todolist.exception;

import java.io.Serial;

public class TodolistNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public TodolistNotFoundException(String msg) {
        super(msg);
    }
}
