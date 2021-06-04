package com.kds.ourmemory.advice.v1.notice.exception;

public class NoticeInternalServerException extends RuntimeException {
    public NoticeInternalServerException(String msg, Throwable t) {
        super(msg, t);
    }

    public NoticeInternalServerException(String msg) {
        super(msg);
    }

    public NoticeInternalServerException() {
        super();
    }
}
