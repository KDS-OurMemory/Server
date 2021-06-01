package com.kds.ourmemory.advice.v1.notice.exception;

public class NoticeNotFoundUserException extends RuntimeException {
    public NoticeNotFoundUserException(String msg, Throwable t) {
        super(msg, t);
    }

    public NoticeNotFoundUserException(String msg) {
        super(msg);
    }

    public NoticeNotFoundUserException() {
        super();
    }
}
