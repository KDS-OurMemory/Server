package com.kds.ourmemory.advice.v1.room;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResult;
import com.kds.ourmemory.advice.v1.room.exception.RoomAddMemberException;
import com.kds.ourmemory.advice.v1.room.exception.RoomInternalServerException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundOwnerException;
import com.kds.ourmemory.controller.v1.room.RoomController;

import lombok.extern.slf4j.Slf4j;

/**
 * Because the communication was successful, the status code value is set to 200 
 * and the error code value and message are passed.
 * 
 * @author idean
 */
@Slf4j
@RestControllerAdvice(assignableTypes = RoomController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RoomControllerAdvice extends RestControllerAdviceResult{

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<?> handleRoomNotFoundException(RoomNotFoundException e) {
        log.warn(e.getMessage());
        return response(RoomResultCode.NOT_FOUND);
    }
    
    @ExceptionHandler(RoomNotFoundOwnerException.class)
    public ResponseEntity<?> handleRoomNotFoundOwnerException(RoomNotFoundOwnerException e) {
        log.warn(e.getMessage());
        return response(RoomResultCode.NOT_FOUND_OWNER);
    }
    
    @ExceptionHandler(RoomAddMemberException.class)
    public ResponseEntity<?> handleRoomAddMemberException(RoomAddMemberException e) {
        log.warn(e.getMessage());
        return response(RoomResultCode.ADD_MEMBER_ERROR);
    }
    
    @ExceptionHandler(RoomInternalServerException.class)
    public ResponseEntity<?> handleRoomInternalServerException(RoomInternalServerException e) {
        log.warn(e.getMessage(), e);
        return response(RoomResultCode.INTERNAL_SERVER_ERROR);
    }
}
