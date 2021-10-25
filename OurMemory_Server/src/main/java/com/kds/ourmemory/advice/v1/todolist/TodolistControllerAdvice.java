package com.kds.ourmemory.advice.v1.todolist;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResponse;
import com.kds.ourmemory.advice.v1.todolist.exception.TodolistInternalServerException;
import com.kds.ourmemory.advice.v1.todolist.exception.TodolistNotFoundException;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.todolist.TodolistController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.kds.ourmemory.advice.v1.todolist.TodolistResultCode.*;

/**
 * Because the communication was successful, the status code value is set to 200.
 * And the error code value and message are passed.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = TodolistController.class)
public class TodolistControllerAdvice extends RestControllerAdviceResponse {

    /* Custom Error */
    // TODO: 로직에서 커스텀 예외 발생 시, 추가바람.

    /* Http Status Error */
    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResult<String>> handleBadRequestException(Exception e) {
        return response(BAD_REQUEST, e);
    }

    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiResult<String>> handleCustomBadRequestException(Exception e) {
        return response(BAD_REQUEST.getCode(), e.getMessage(), e);
    }

    @ExceptionHandler(TodolistNotFoundException.class)
    public ResponseEntity<ApiResult<String>> handleTodolistNotFoundException(TodolistNotFoundException e) {
        return response(NOT_FOUND, e);
    }

    @ExceptionHandler(TodolistInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleTodolistInternalServerException(TodolistInternalServerException e) {
        return response(INTERNAL_SERVER_ERROR, e);
    }
}
