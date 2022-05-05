package com.kds.ourmemory.notice.v1.advice.exception;

import com.kds.ourmemory.common.v1.advice.ArgsRuntimeException;

public class NoticeNotFoundUserException extends ArgsRuntimeException {

    public NoticeNotFoundUserException(Object... args) {
        super(args);
    }

}
