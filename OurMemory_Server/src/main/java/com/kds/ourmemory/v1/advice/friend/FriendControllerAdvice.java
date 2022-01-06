package com.kds.ourmemory.v1.advice.friend;

import com.kds.ourmemory.v1.advice.RestControllerAdviceResponse;
import com.kds.ourmemory.v1.advice.friend.exception.*;
import com.kds.ourmemory.v1.controller.ApiResult;
import com.kds.ourmemory.v1.controller.friend.FriendController;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Because the communication was successful, the status code value is set to 200.
 * And the error code value and message are passed.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = FriendController.class)
public class FriendControllerAdvice extends RestControllerAdviceResponse {

    public FriendControllerAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    /* Custom Error */
    @ExceptionHandler(FriendNotFoundUserException.class)
    public ResponseEntity<ApiResult<String>> handleFriendNotFoundUserException(FriendNotFoundUserException e) {
        return response("friend.notFoundUser", e);
    }

    @ExceptionHandler(FriendNotFoundFriendException.class)
    public ResponseEntity<ApiResult<String>> handleFriendNotFoundFriendException(FriendNotFoundFriendException e) {
        return response("friend.notFoundFriend", e);
    }

    @ExceptionHandler(FriendAlreadyAcceptException.class)
    public ResponseEntity<ApiResult<String>> handleFriendAlreadyAcceptException(FriendAlreadyAcceptException e) {
        return response("friend.alreadyAccept", e);
    }

    @ExceptionHandler(FriendBlockedException.class)
    public ResponseEntity<ApiResult<String>> handleFriendBlockedException(FriendBlockedException e) {
        return response("friend.blockedFromFriend", e);
    }

    @ExceptionHandler(FriendNotRequestedException.class)
    public ResponseEntity<ApiResult<String>> handleFriendNotRequestedException(FriendNotRequestedException e) {
        return response("friend.notRequested", e);
    }

    @ExceptionHandler(FriendStatusException.class)
    public ResponseEntity<ApiResult<String>> handleFriendStatusException(FriendStatusException e) {
        return response("friend.statusError", e);
    }


    /* Http Status Error */
    @ExceptionHandler({ MissingServletRequestParameterException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, IllegalStateException.class })
    public ResponseEntity<ApiResult<String>> handleBadRequestException(Exception e) {
        return response("friend.badRequest", e);
    }

    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiResult<String>> handleCustomBadRequestException(Exception e) {
        return response("friend.badRequest", e);
    }

    @ExceptionHandler(FriendNotFoundException.class)
    public ResponseEntity<ApiResult<String>> handleFriendNotFoundException(FriendNotFoundException e) {
        return response("friend.notFound", e);
    }

    @ExceptionHandler(FriendInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleFriendInternalServerException(FriendInternalServerException e) {
        return response("friend.internalServer", e);
    }

}
