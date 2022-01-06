package com.kds.ourmemory.v1.advice.relation;

import com.kds.ourmemory.v1.advice.RestControllerAdviceResponse;
import com.kds.ourmemory.v1.advice.relation.exception.UserMemoryInternalServerException;
import com.kds.ourmemory.v1.controller.ApiResult;
import com.kds.ourmemory.v1.controller.memory.MemoryController;
import com.kds.ourmemory.v1.controller.user.UserController;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = {UserController.class, MemoryController.class})
public class UserMemoryControllerAdvice extends RestControllerAdviceResponse {

    public UserMemoryControllerAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    /* Http Status Error */
    @ExceptionHandler(UserMemoryInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleUserMemoryInternalServerException(UserMemoryInternalServerException e) {
        return response("relation.userMemory.internalServer", e);
    }
}
