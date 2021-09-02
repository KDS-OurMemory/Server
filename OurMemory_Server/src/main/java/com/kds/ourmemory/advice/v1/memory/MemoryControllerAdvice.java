package com.kds.ourmemory.advice.v1.memory;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResponse;
import com.kds.ourmemory.advice.v1.memory.exception.*;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.memory.MemoryController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.kds.ourmemory.advice.v1.memory.MemoryResultCode.*;

/**
 * Because the communication was successful, the status code value is set to 200.
 * And the error code value and message are passed.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = MemoryController.class)
public class MemoryControllerAdvice extends RestControllerAdviceResponse{
    
    /* Custom Error */
    @ExceptionHandler(MemoryNotFoundWriterException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryNotFoundWriterException(MemoryNotFoundWriterException e) {
        return response(NOT_FOUND_WRITER, e);
    }

    @ExceptionHandler(MemoryNotFoundShareMemberException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryNotFoundMemberException(MemoryNotFoundShareMemberException e) {
        return response(NOT_FOUND_SHARE_MEMBER, e);
    }
    
    @ExceptionHandler(MemoryNotFoundRoomException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryNotFoundRoomException(MemoryNotFoundRoomException e) {
        return response(NOT_FOUND_ROOM, e);
    }
    
    
    /* HTTP Status Error */
    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResult<String>> handleBadRequestException(Exception e) {
        return response(BAD_REQUEST, e);
    }
    
    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiResult<String>> handleCustomBadRequestException(Exception e) {
        return response(BAD_REQUEST.getCode(), e.getMessage(), e);
    }
    
    @ExceptionHandler(MemoryNotFoundException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryNotFoundException(MemoryNotFoundException e) {
        return response(NOT_FOUND, e);
    }
    
    @ExceptionHandler(MemoryInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryInternalServerException(MemoryInternalServerException e) {
        return response(INTERNAL_SERVER_ERROR, e);
    }
}
