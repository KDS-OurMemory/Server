package com.kds.ourmemory.advice.v1.memory;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResult;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryInternalServerException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.controller.v1.memory.MemoryController;

import lombok.extern.slf4j.Slf4j;

/**
 * Because the communication was successful, the status code value is set to 200 
 * and the error code value and message are passed.
 * 
 * @author idean
 */
@Slf4j
@RestControllerAdvice(assignableTypes = MemoryController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MemoryControllerAdvice extends RestControllerAdviceResult{
    @ExceptionHandler(MemoryNotFoundException.class)
    public ResponseEntity<?> handleMemoryNotFoundException(MemoryNotFoundException e) {
        log.warn(e.getMessage());
        return response(MemoryResultCode.NOT_FOUND);
    }
    
    @ExceptionHandler(MemoryInternalServerException.class)
    public ResponseEntity<?> handleMemoryInternalServerException(MemoryInternalServerException e) {
        log.warn(e.getMessage(), e);
        return response(MemoryResultCode.INTERNAL_SERVER);
    }
}
