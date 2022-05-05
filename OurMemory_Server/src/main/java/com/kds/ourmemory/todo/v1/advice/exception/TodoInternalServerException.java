package com.kds.ourmemory.todo.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class TodoInternalServerException extends ArgsRuntimeException {

    public TodoInternalServerException(Object... args) {
        super(args);
    }

}
