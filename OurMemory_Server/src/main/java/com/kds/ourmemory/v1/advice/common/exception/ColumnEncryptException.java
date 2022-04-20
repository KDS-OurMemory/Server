package com.kds.ourmemory.v1.advice.common.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class ColumnEncryptException extends ArgsRuntimeException {
    public ColumnEncryptException(Object... args) {
        super(args);
    }
}
