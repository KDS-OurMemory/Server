package com.kds.ourmemory.v1.advice.user;

import com.kds.ourmemory.v1.advice.RestControllerAdviceResponse;
import com.kds.ourmemory.v1.advice.user.exception.UserInternalServerException;
import com.kds.ourmemory.v1.advice.user.exception.UserNotFoundException;
import com.kds.ourmemory.v1.advice.user.exception.UserProfileImageUploadException;
import com.kds.ourmemory.v1.controller.ApiResult;
import com.kds.ourmemory.v1.controller.user.UserController;
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
@RestControllerAdvice(assignableTypes = UserController.class)
public class UserControllerAdvice extends RestControllerAdviceResponse {

    public UserControllerAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    /* Custom Error */
    @ExceptionHandler(UserProfileImageUploadException.class)
    public ResponseEntity<ApiResult<String>> handleUserProfileImageUploadException(UserProfileImageUploadException e) {
        return response("user.profileUploadError", e);
    }

    /* Http Status Error */
    @ExceptionHandler({ MissingServletRequestParameterException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, IllegalStateException.class })
    public ResponseEntity<ApiResult<String>> handleBadRequestException(Exception e) {
        return response("user.badRequest", e);
    }

    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiResult<String>> handleCustomBadRequestException(Exception e) {
        return response("user.badRequest", e);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResult<String>> handleUserNotFoundException(UserNotFoundException e) {
        return response("user.notFound", e);
    }

    @ExceptionHandler(UserInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleUserInternalServerException(UserInternalServerException e) {
        return response("user.internalServerError", e);
    }

}
