package com.kds.ourmemory.relation.v1.advice;

import com.kds.ourmemory.common.v1.advice.RestControllerAdviceResponse;
import com.kds.ourmemory.relation.v1.advice.exception.UserMemoryInternalServerException;
import com.kds.ourmemory.common.v1.controller.ApiResult;
import com.kds.ourmemory.memory.v1.controller.MemoryController;
import com.kds.ourmemory.user.v1.controller.UserController;
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
