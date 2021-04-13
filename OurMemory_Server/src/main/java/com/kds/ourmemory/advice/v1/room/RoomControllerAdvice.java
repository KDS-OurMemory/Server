package com.kds.ourmemory.advice.v1.room;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResult;
import com.kds.ourmemory.advice.v1.room.exception.RoomDataRelationException;
import com.kds.ourmemory.advice.v1.room.exception.RoomInternalServerException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundOwnerException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNullException;
import com.kds.ourmemory.controller.v1.room.RoomController;

/**
 * Because the communication was successful, the status code value is set to 200 
 * and the error code value and message are passed.
 * 
 * @author idean
 */
@RestControllerAdvice(assignableTypes = RoomController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RoomControllerAdvice extends RestControllerAdviceResult{

    /* Custom Error */
    @ExceptionHandler(RoomDataRelationException.class)
    public ResponseEntity<?> handleRoomAddMemberException(RoomDataRelationException e) {
        return response(RoomResultCode.DATA_RELATION_ERROR, e);
    }
    
    @ExceptionHandler(RoomNotFoundOwnerException.class)
    public ResponseEntity<?> handleRoomNotFoundOwnerException(RoomNotFoundOwnerException e) {
        return response(RoomResultCode.NOT_FOUND_OWNER, e);
    }
    
    @ExceptionHandler(RoomNullException.class)
    public ResponseEntity<?> handleRoomNullException(RoomNullException e) {
        return response(RoomResultCode.ROOM_NULL_ERROR, e);
    }
    
    /* Http Status Error */
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<?> handleRoomNotFoundException(RoomNotFoundException e) {
        return response(RoomResultCode.NOT_FOUND, e);
    }
    
    @ExceptionHandler(RoomInternalServerException.class)
    public ResponseEntity<?> handleRoomInternalServerException(RoomInternalServerException e) {
        return response(RoomResultCode.INTERNAL_SERVER_ERROR, e);
    }
}
