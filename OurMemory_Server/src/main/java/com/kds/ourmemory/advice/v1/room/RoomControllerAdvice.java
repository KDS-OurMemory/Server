package com.kds.ourmemory.advice.v1.room;

import javax.validation.UnexpectedTypeException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResult;
import com.kds.ourmemory.advice.v1.room.exception.RoomInternalServerException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundMemberException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundOwnerException;
import com.kds.ourmemory.controller.v1.room.RoomController;

/**
 * Because the communication was successful, the status code value is set to 200.
 * And the error code value and message are passed.
 */
@RestControllerAdvice(assignableTypes = RoomController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RoomControllerAdvice extends RestControllerAdviceResult{

    /* Custom Error */
    @ExceptionHandler(RoomNotFoundOwnerException.class)
    public ResponseEntity<?> handleRoomNotFoundOwnerException(RoomNotFoundOwnerException e) {
        return response(RoomResultCode.NOT_FOUND_OWNER, e);
    }
    
    @ExceptionHandler(RoomNotFoundMemberException.class)
    public ResponseEntity<?> handleRoomNotFoundMemberException (RoomNotFoundMemberException e) {
        return response(RoomResultCode.NOT_FOUND_MEMBER, e);
    }
    
    
    /* Http Status Error */
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class, UnexpectedTypeException.class})
    public ResponseEntity<?> handleMethodArgumentNotValidException(Exception e) {
        return response(RoomResultCode.BAD_PARAMETER, e);
    }
    
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<?> handleRoomNotFoundException(RoomNotFoundException e) {
        return response(RoomResultCode.NOT_FOUND, e);
    }
    
    @ExceptionHandler(RoomInternalServerException.class)
    public ResponseEntity<?> handleRoomInternalServerException(RoomInternalServerException e) {
        return response(RoomResultCode.INTERNAL_SERVER_ERROR, e);
    }
}
