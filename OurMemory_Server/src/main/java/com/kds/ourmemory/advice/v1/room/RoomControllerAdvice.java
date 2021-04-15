package com.kds.ourmemory.advice.v1.room;

import static com.kds.ourmemory.advice.v1.room.RoomResultCode.BAD_REQUEST;
import static com.kds.ourmemory.advice.v1.room.RoomResultCode.INTERNAL_SERVER_ERROR;
import static com.kds.ourmemory.advice.v1.room.RoomResultCode.NOT_FOUND;
import static com.kds.ourmemory.advice.v1.room.RoomResultCode.NOT_FOUND_MEMBER;
import static com.kds.ourmemory.advice.v1.room.RoomResultCode.NOT_FOUND_OWNER;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResponse;
import com.kds.ourmemory.advice.v1.room.exception.RoomInternalServerException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundMemberException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundOwnerException;
import com.kds.ourmemory.controller.v1.room.RoomController;

/**
 * Because the communication was successful, the status code value is set to 200.
 * And the error code value and message are passed.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = RoomController.class)
public class RoomControllerAdvice extends RestControllerAdviceResponse{

    /* Custom Error */
    @ExceptionHandler(RoomNotFoundOwnerException.class)
    public ResponseEntity<?> handleRoomNotFoundOwnerException(RoomNotFoundOwnerException e) {
        return response(NOT_FOUND_OWNER, e);
    }
    
    @ExceptionHandler(RoomNotFoundMemberException.class)
    public ResponseEntity<?> handleRoomNotFoundMemberException (RoomNotFoundMemberException e) {
        return response(NOT_FOUND_MEMBER, e);
    }
    
    
    /* Http Status Error */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<?> handleBadRequestException(Exception e) {
        return response(BAD_REQUEST, e);
    }
    
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<?> handleRoomNotFoundException(RoomNotFoundException e) {
        return response(NOT_FOUND, e);
    }
    
    @ExceptionHandler(RoomInternalServerException.class)
    public ResponseEntity<?> handleRoomInternalServerException(RoomInternalServerException e) {
        return response(INTERNAL_SERVER_ERROR, e);
    }
}
