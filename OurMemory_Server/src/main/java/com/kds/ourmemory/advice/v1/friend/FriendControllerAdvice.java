package com.kds.ourmemory.advice.v1.friend;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResponse;
import com.kds.ourmemory.advice.v1.friend.exception.FriendInternalServerException;
import com.kds.ourmemory.advice.v1.friend.exception.FriendNotFoundFriendException;
import com.kds.ourmemory.advice.v1.friend.exception.FriendNotFoundUserException;
import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
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
    public ResponseEntity<?> handleFriendNotFoundUserException(FriendNotFoundUserException e) {
        return response(NOT_FOUND_USER, e);
    }

    @ExceptionHandler(FriendNotFoundFriendException.class)
    public ResponseEntity<?> handleFriendNotFoundFriendException (FriendNotFoundFriendException e) {
        return response(NOT_FOUND_FRIEND, e);
    }


    /* Http Status Error */
    @ExceptionHandler({ MissingServletRequestParameterException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, IllegalStateException.class })
    public ResponseEntity<?> handleBadRequestException(Exception e) {
        return response(BAD_REQUEST, e);
    }

    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class })
    public ResponseEntity<?> handleCustomBadRequestException(Exception e) {
        return response(BAD_REQUEST.getCode(), e.getMessage(), e);
    }

    @ExceptionHandler(FriendInternalServerException.class)
    public ResponseEntity<?> handleUserInternalServerException(UserInternalServerException e) {
        return response(INTERNAL_SERVER_ERROR, e);
    }
}
