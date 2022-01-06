package com.kds.ourmemory.v1.advice.todo.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class TodoInternalServerException extends ArgsRuntimeException {

    public TodoInternalServerException(Object... args) {
        super(args);
    }

}
