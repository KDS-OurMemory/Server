package com.kds.ourmemory.advice.v1.friend;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResponse;
import com.kds.ourmemory.advice.v1.friend.exception.*;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.friend.FriendController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static com.kds.ourmemory.advice.v1.friend.FriendResultCode.*;

/**
 * Because the communication was successful, the status code value is set to 200.
 * And the error code value and message are passed.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = FriendController.class)
public class FriendControllerAdvice extends RestControllerAdviceResponse {

    /* Custom Error */
    @ExceptionHandler(FriendNotFoundUserException.class)
    public ResponseEntity<ApiResult<String>> handleFriendNotFoundUserException(FriendNotFoundUserException e) {
        return response(NOT_FOUND_USER, e);
    }

    @ExceptionHandler(FriendNotFoundFriendException.class)
    public ResponseEntity<ApiResult<String>> handleFriendNotFoundFriendException(FriendNotFoundFriendException e) {
        return response(NOT_FOUND_FRIEND, e);
    }

    @ExceptionHandler(FriendAlreadyAcceptException.class)
    public ResponseEntity<ApiResult<String>> handleFriendAlreadyAcceptException(FriendAlreadyAcceptException e) {
        return response(ALREADY_ACCEPT, e);
    }

    @ExceptionHandler(FriendBlockedException.class)
    public ResponseEntity<ApiResult<String>> handleFriendBlockedException(FriendBlockedException e) {
        return response(BLOCKED_FROM_FRIEND, e);
    }

    @ExceptionHandler(FriendNotRequestedException.class)
    public ResponseEntity<ApiResult<String>> handleFriendNotRequestedException(FriendNotRequestedException e) {
        return response(NOT_REQUESTED, e);
    }

    @ExceptionHandler(FriendStatusException.class)
    public ResponseEntity<ApiResult<String>> handleFriendStatusException(FriendStatusException e) {
        return response(STATUS_ERROR, e);
    }


    /* Http Status Error */
    @ExceptionHandler({ MissingServletRequestParameterException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, IllegalStateException.class })
    public ResponseEntity<ApiResult<String>> handleBadRequestException(Exception e) {
        return response(BAD_REQUEST, e);
    }

    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiResult<String>> handleCustomBadRequestException(Exception e) {
        return response(BAD_REQUEST.getCode(), e.getMessage(), e);
    }

    @ExceptionHandler(FriendNotFoundException.class)
    public ResponseEntity<ApiResult<String>> handleFriendNotFoundException(FriendNotFoundException e) {
        return response(NOT_FOUND, e);
    }

    @ExceptionHandler(FriendInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleFriendInternalServerException(FriendInternalServerException e) {
        return response(INTERNAL_SERVER_ERROR, e);
    }
}
