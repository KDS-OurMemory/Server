package com.kds.ourmemory.advice.v1.memory;

import javax.validation.UnexpectedTypeException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResult;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryDataRelationException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryInternalServerException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundRoomException;
import com.kds.ourmemory.advice.v1.memory.exception.MemoryNotFoundWriterException;
import com.kds.ourmemory.controller.v1.memory.MemoryController;

/**
 * Because the communication was successful, the status code value is set to 200 
 * and the error code value and message are passed.
 * 
 * @author idean
 */
@RestControllerAdvice(assignableTypes = MemoryController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MemoryControllerAdvice extends RestControllerAdviceResult{
    
    /* Custom Error */
    @ExceptionHandler(MemoryDataRelationException.class)
    public ResponseEntity<?> handleMemoryDataRelationException(MemoryDataRelationException e) {
        return response(MemoryResultCode.DATA_RELATION_ERROR, e);
    }
    
    @ExceptionHandler(MemoryNotFoundWriterException.class)
    public ResponseEntity<?> handleRoomNotFoundOwnerException(MemoryNotFoundWriterException e) {
        return response(MemoryResultCode.NOT_FOUND_WRITER, e);
    }
    
    @ExceptionHandler(MemoryNotFoundRoomException.class)
    public ResponseEntity<?> handleMemoryNotFoundRoomException(MemoryNotFoundRoomException e) {
        return response(MemoryResultCode.NOT_FOUND_ROOM, e);
    }
    
    
    /* HTTP Status Error */
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class, UnexpectedTypeException.class})
    public ResponseEntity<?> handleMethodArgumentNotValidException(Exception e) {
        return response(MemoryResultCode.BAD_PARAMETER, e);
    }
    
    @ExceptionHandler(MemoryNotFoundException.class)
    public ResponseEntity<?> handleMemoryNotFoundException(MemoryNotFoundException e) {
        return response(MemoryResultCode.NOT_FOUND, e);
    }
    
    @ExceptionHandler(MemoryInternalServerException.class)
    public ResponseEntity<?> handleMemoryInternalServerException(MemoryInternalServerException e) {
        return response(MemoryResultCode.INTERNAL_SERVER_ERROR, e);
    }
}
