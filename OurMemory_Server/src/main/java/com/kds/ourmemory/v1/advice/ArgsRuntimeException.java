package com.kds.ourmemory.v1.advice;

import lombok.Getter;

@Getter
public class ArgsRuntimeException extends RuntimeException{

    private final Object[] args;

    public ArgsRuntimeException() {
        super();
        args = new Object[0];
    }

    public ArgsRuntimeException(Object[] args) {
        super();
        this.args = args;
    }

    public ArgsRuntimeException(String msg) {
        super(msg);
        args = new Object[]{ msg };
    }

}
