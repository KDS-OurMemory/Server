package com.kds.ourmemory.v1.advice.todo;

import com.kds.ourmemory.v1.advice.RestControllerAdviceResponse;
import com.kds.ourmemory.v1.advice.todo.exception.TodoInternalServerException;
import com.kds.ourmemory.v1.advice.todo.exception.TodoNotFoundException;
import com.kds.ourmemory.v1.controller.ApiResult;
import com.kds.ourmemory.v1.controller.todo.TodoController;
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
@RestControllerAdvice(assignableTypes = TodoController.class)
public class TodoControllerAdvice extends RestControllerAdviceResponse {
    public TodoControllerAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    /* Http Status Error */
    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResult<String>> handleBadRequestException(Exception e) {
        return response("todo.badRequest", e);
    }

    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiResult<String>> handleCustomBadRequestException(Exception e) {
        return response("todo.badRequest", e);
    }

    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<ApiResult<String>> handleTodolistNotFoundException(TodoNotFoundException e) {
        return response("todo.notFound", e);
    }

    @ExceptionHandler(TodoInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleTodolistInternalServerException(TodoInternalServerException e) {
        return response("todo.internalServer", e);
    }
}
