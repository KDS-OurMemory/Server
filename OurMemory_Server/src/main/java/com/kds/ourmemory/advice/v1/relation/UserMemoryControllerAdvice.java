package com.kds.ourmemory.advice.v1.relation;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResponse;
import com.kds.ourmemory.advice.v1.relation.exception.UserMemoryInternalServerException;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.memory.MemoryController;
import com.kds.ourmemory.controller.v1.user.UserController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.kds.ourmemory.advice.v1.relation.UserMemoryResultCode.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = {UserController.class, MemoryController.class})
public class UserMemoryControllerAdvice extends RestControllerAdviceResponse {

    /* Http Status Error */
    @ExceptionHandler(UserMemoryInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleUserMemoryInternalServerException(UserMemoryInternalServerException e) {
        return response(INTERNAL_SERVER_ERROR, e);
    }
}
