package com.kds.ourmemory.v1.advice.notice.exception;

import com.kds.ourmemory.v1.advice.ArgsRuntimeException;

public class NoticeNotFoundUserException extends ArgsRuntimeException {

    public NoticeNotFoundUserException(Object... args) {
        super(args);
    }

}
