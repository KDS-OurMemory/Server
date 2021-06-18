package com.kds.ourmemory.advice.v1.room;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResponse;
import com.kds.ourmemory.advice.v1.room.exception.RoomInternalServerException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundMemberException;
import com.kds.ourmemory.advice.v1.room.exception.RoomNotFoundOwnerException;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.room.RoomController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.kds.ourmemory.advice.v1.room.RoomResultCode.*;

/**
 * Because the communication was successful, the status code value is set to 200.
 * And the error code value and message are passed.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = RoomController.class)
public class RoomControllerAdvice extends RestControllerAdviceResponse{

    /* Custom Error */
    @ExceptionHandler(RoomNotFoundOwnerException.class)
    public ResponseEntity<ApiResult<String>> handleRoomNotFoundOwnerException(RoomNotFoundOwnerException e) {
        return response(NOT_FOUND_OWNER, e);
    }
    
    @ExceptionHandler(RoomNotFoundMemberException.class)
    public ResponseEntity<ApiResult<String>> handleRoomNotFoundMemberException (RoomNotFoundMemberException e) {
        return response(NOT_FOUND_MEMBER, e);
    }
    
    
    /* Http Status Error */
    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResult<String>> handleBadRequestException(Exception e) {
        return response(BAD_REQUEST, e);
    }
    
    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiResult<String>> handleCustomBadRequestException(Exception e) {
        return response(BAD_REQUEST.getCode(), e.getMessage(), e);
    }
    
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ApiResult<String>> handleRoomNotFoundException(RoomNotFoundException e) {
        return response(NOT_FOUND, e);
    }
    
    @ExceptionHandler(RoomInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleRoomInternalServerException(RoomInternalServerException e) {
        return response(INTERNAL_SERVER_ERROR, e);
    }
}
