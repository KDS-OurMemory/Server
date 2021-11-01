package com.kds.ourmemory.advice.v1.todo;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResponse;
import com.kds.ourmemory.advice.v1.todo.exception.TodoInternalServerException;
import com.kds.ourmemory.advice.v1.todo.exception.TodoNotFoundException;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.todo.TodoController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.kds.ourmemory.advice.v1.todo.TodoResultCode.*;

/**
 * Because the communication was successful, the status code value is set to 200.
 * And the error code value and message are passed.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = TodoController.class)
public class TodoControllerAdvice extends RestControllerAdviceResponse {

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

    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<ApiResult<String>> handleTodolistNotFoundException(TodoNotFoundException e) {
        return response(NOT_FOUND, e);
    }

    @ExceptionHandler(TodoInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleTodolistInternalServerException(TodoInternalServerException e) {
        return response(INTERNAL_SERVER_ERROR, e);
    }
}
