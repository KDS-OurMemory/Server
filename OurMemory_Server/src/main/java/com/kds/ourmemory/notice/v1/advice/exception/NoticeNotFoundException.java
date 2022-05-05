package com.kds.ourmemory.notice.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class NoticeNotFoundException extends ArgsRuntimeException {

    public NoticeNotFoundException(Object... args) {
        super(args);
    }

}
