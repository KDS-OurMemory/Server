package com.kds.ourmemory.advice.v1.user;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResult;
import com.kds.ourmemory.advice.v1.user.exception.UserInternalServerException;
import com.kds.ourmemory.advice.v1.user.exception.UserNotFoundException;
import com.kds.ourmemory.advice.v1.user.exception.UserTokenUpdateException;
import com.kds.ourmemory.controller.v1.user.UserController;

/**
 * Because the communication was successful, the status code value is set to 200 
 * and the error code value and message are passed.
 * 
 * @author idean
 */
@RestControllerAdvice(assignableTypes = UserController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserControllerAdvice extends RestControllerAdviceResult{
    
    @ExceptionHandler(UserTokenUpdateException.class)
    public ResponseEntity<?> handleUserTokenUpdateException(UserTokenUpdateException e) {
        return response(UserResultCode.TOKEN_UPDATE_ERROR, e);
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException e) {
        return response(UserResultCode.NOT_FOUND, e);
    }
    
    @ExceptionHandler(UserInternalServerException.class)
    public ResponseEntity<?> handleUserInternalServerException(UserInternalServerException e) {
        return response(UserResultCode.INTERNAL_SERVER_ERROR, e);
    }
}
