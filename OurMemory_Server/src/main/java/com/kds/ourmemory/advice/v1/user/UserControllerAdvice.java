package com.kds.ourmemory.advice.v1.user;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResponse;
import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.controller.v1.user.UserController;

import static com.kds.ourmemory.advice.v1.user.UserResultCode.*;

/**
 * Because the communication was successful, the status code value is set to
 * 200. And the error code value and message are passed.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = UserController.class)
public class UserControllerAdvice extends RestControllerAdviceResponse {

    /* Http Status Error */
    @ExceptionHandler({ MissingServletRequestParameterException.class, HttpMessageNotReadableException.class })
    public ResponseEntity<?> handleBadRequestException(Exception e) {
        return response(BAD_REQUEST, e);
    }
    
    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class })
    public ResponseEntity<?> handleCustomBadRequestException(Exception e) {
        return response(BAD_REQUEST.getCode(), e.getMessage(), e);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException e) {
        return response(NOT_FOUND, e);
    }

    @ExceptionHandler(UserInternalServerException.class)
    public ResponseEntity<?> handleUserInternalServerException(UserInternalServerException e) {
        return response(INTERNAL_SERVER_ERROR, e);
    }
}
