package com.kds.ourmemory.v1.advice.todo.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class TodoNotFoundException extends ArgsRuntimeException {

    public TodoNotFoundException(Object... args) {
        super(args);
    }

}
