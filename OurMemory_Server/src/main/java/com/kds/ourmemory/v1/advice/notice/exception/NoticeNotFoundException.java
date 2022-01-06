package com.kds.ourmemory.v1.advice.notice.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class NoticeNotFoundException extends ArgsRuntimeException {

    public NoticeNotFoundException(Object... args) {
        super(args);
    }

}
