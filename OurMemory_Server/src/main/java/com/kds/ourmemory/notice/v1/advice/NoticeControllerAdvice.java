package com.kds.ourmemory.notice.v1.advice;

import com.kds.ourmemory.notice.v1.advice.exception.NoticeNotFoundException;
import com.kds.ourmemory.notice.v1.advice.exception.NoticeNotFoundUserException;
import com.kds.ourmemory.common.v1.advice.RestControllerAdviceResponse;
import com.kds.ourmemory.notice.v1.advice.exception.NoticeInternalServerException;
import com.kds.ourmemory.common.v1.controller.ApiResult;
import com.kds.ourmemory.notice.v1.controller.NoticeController;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Because the communication was successful, the status code value is set to 200.
 * And the error code value and message are passed.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = NoticeController.class)
public class NoticeControllerAdvice extends RestControllerAdviceResponse {

    public NoticeControllerAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    /* Custom Error */
    @ExceptionHandler(NoticeNotFoundUserException.class)
    public ResponseEntity<ApiResult<String>> handleNoticeNotFoundUserException(NoticeNotFoundUserException e) {
        return response("notice.notFoundUser", e);
    }

    /* HTTP Status Error */
    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResult<String>> handleBadRequestException(Exception e) {
        return response("notice.badRequest", e);
    }

    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiResult<String>> handleCustomBadRequestException(Exception e) {
        return response("notice.badRequest", e);
    }

    @ExceptionHandler(NoticeNotFoundException.class)
    public ResponseEntity<ApiResult<String>> handleNoticeNotFoundException(NoticeNotFoundException e) {
        return response("notice.notFound", e);
    }

    @ExceptionHandler(NoticeInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleNoticeInternalServerException(NoticeInternalServerException e) {
        return response("notice.internalServer", e);
    }

}
