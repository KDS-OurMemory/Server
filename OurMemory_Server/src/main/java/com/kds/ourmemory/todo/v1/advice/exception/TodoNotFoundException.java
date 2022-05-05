package com.kds.ourmemory.todo.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class TodoNotFoundException extends ArgsRuntimeException {

    public TodoNotFoundException(Object... args) {
        super(args);
    }

}
