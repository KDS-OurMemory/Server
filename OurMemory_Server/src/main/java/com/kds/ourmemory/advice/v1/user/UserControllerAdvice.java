package com.kds.ourmemory.advice.v1.user;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResult;
import com.kds.ourmemory.advice.v1.user.exception.UserInterServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.advice.v1.user.exception.UserPatchTokenException;
import com.kds.ourmemory.controller.v1.user.UserController;

import lombok.extern.slf4j.Slf4j;

/**
 * Because the communication was successful, the status code value is set to 200 
 * and the error code value and message are passed.
 * 
 * @author idean
 */
@Slf4j
@RestControllerAdvice(assignableTypes = UserController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserControllerAdvice extends RestControllerAdviceResult{
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException e) {
        log.warn(e.getMessage());
        return response(UserResultCode.NOT_FOUND);
    }
    
    @ExceptionHandler(UserPatchTokenException.class)
    public ResponseEntity<?> handleUserPatchTokenException(UserPatchTokenException e) {
        log.warn(e.getMessage());
        return response(UserResultCode.PATCH_TOKEN_ERROR);
    }
    
    @ExceptionHandler(UserInterServerException.class)
    public ResponseEntity<?> handleUserInternalServerException(UserInterServerException e) {
        log.warn(e.getMessage(), e);
        return response(UserResultCode.INTERNAL_SERVER_ERROR);
    }
}
