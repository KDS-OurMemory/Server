package com.kds.ourmemory.advice.v1.notice;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResponse;
import com.kds.ourmemory.advice.v1.notice.exception.NoticeInternalServerException;
import com.kds.ourmemory.advice.v1.notice.exception.NoticeNotFoundException;
import com.kds.ourmemory.advice.v1.notice.exception.NoticeNotFoundUserException;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.notice.NoticeController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.kds.ourmemory.advice.v1.notice.NoticeResultCode.*;

/**
 * Because the communication was successful, the status code value is set to 200.
 * And the error code value and message are passed.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = NoticeController.class)
public class NoticeControllerAdvice extends RestControllerAdviceResponse {

    /* Custom Error */
    @ExceptionHandler(NoticeNotFoundUserException.class)
    public ResponseEntity<ApiResult<String>> handleNoticeNotFoundUserException(NoticeNotFoundUserException e) {
        return response(NOT_FOUND_USER, e);
    }

    /* HTTP Status Error */
    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResult<String>> handleBadRequestException(Exception e) {
        return response(BAD_REQUEST, e);
    }

    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiResult<String>> handleCustomBadRequestException(Exception e) {
        return response(BAD_REQUEST.getCode(), e.getMessage(), e);
    }

    @ExceptionHandler(NoticeNotFoundException.class)
    public ResponseEntity<ApiResult<String>> handleNoticeNotFoundException(NoticeNotFoundException e) {
        return response(NOT_FOUND, e);
    }

    @ExceptionHandler(NoticeInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleNoticeInternalServerException(NoticeInternalServerException e) {
        return response(INTERNAL_SERVER_ERROR, e);
    }
}
