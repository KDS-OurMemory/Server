package com.kds.ourmemory.v1.advice.memory;

import com.kds.ourmemory.v1.advice.RestControllerAdviceResponse;
import com.kds.ourmemory.v1.advice.memory.exception.*;
import com.kds.ourmemory.v1.controller.ApiResult;
import com.kds.ourmemory.v1.controller.memory.MemoryController;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Because the communication was successful, the status code value is set to 200.
 * And the error code value and message are passed.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = MemoryController.class)
public class MemoryControllerAdvice extends RestControllerAdviceResponse {

    public MemoryControllerAdvice(MessageSource messageSource) {
        super(messageSource);
    }


    /* HTTP Status Error */
    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResult<String>> handleBadRequestException(Exception e) {
        return response("memory.badRequest", e);
    }

    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiResult<String>> handleCustomBadRequestException(Exception e) {
        return response("memory.badRequest", e);
    }

    @ExceptionHandler(MemoryNotFoundException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryNotFoundException(MemoryNotFoundException e) {
        return response("memory.notFound", e);
    }

    @ExceptionHandler(MemoryInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryInternalServerException(MemoryInternalServerException e) {
        return response("memory.internalServer", e);
    }

    /* Custom Error */
    @ExceptionHandler(MemoryNotFoundWriterException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryNotFoundWriterException(MemoryNotFoundWriterException e) {
        return response("memory.notFoundWriter", e);
    }

    @ExceptionHandler(MemoryNotFoundShareMemberException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryNotFoundMemberException(MemoryNotFoundShareMemberException e) {
        return response("memory.notFoundShareMember", e);
    }

    @ExceptionHandler(MemoryNotFoundShareRoomException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryNotFoundShareRoomException(MemoryNotFoundShareRoomException e) {
        return response("memory.notFoundShareRoom", e);
    }
    
    @ExceptionHandler(MemoryNotFoundRoomException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryNotFoundRoomException(MemoryNotFoundRoomException e) {
        return response("memory.notFoundRoom", e);
    }

    @ExceptionHandler(MemoryNotWriterException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryNotWriterException(MemoryNotWriterException e) {
        return response("memory.NotWriter", e);
    }

    @ExceptionHandler(MemoryDeactivateWriterException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryDeactivateWriterException(MemoryDeactivateWriterException e) {
        return response("memory.deactivateWriter", e);
    }

    @ExceptionHandler(MemoryNotIncludeRoomException.class)
    public ResponseEntity<ApiResult<String>> handleMemoryNotIncludeRoomException(MemoryNotIncludeRoomException e) {
        return response("memory.notIncludeRoom", e);
    }

}
