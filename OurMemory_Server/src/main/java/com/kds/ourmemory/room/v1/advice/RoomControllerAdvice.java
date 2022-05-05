package com.kds.ourmemory.room.v1.advice;

import com.kds.ourmemory.room.v1.advice.exception.*;
import com.kds.ourmemory.common.v1.advice.RestControllerAdviceResponse;
import com.kds.ourmemory.common.v1.controller.ApiResult;
import com.kds.ourmemory.room.v1.controller.RoomController;
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
@RestControllerAdvice(assignableTypes = RoomController.class)
public class RoomControllerAdvice extends RestControllerAdviceResponse {

    public RoomControllerAdvice(MessageSource messageSource) {
        super(messageSource);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResult<String>> handleBadRequestException(Exception e) {
        return response("room.badRequest", e);
    }

    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiResult<String>> handleCustomBadRequestException(Exception e) {
        return response("room.badRequest", e);
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ApiResult<String>> handleRoomNotFoundException(RoomNotFoundException e) {
        return response("room.notFound", e);
    }

    @ExceptionHandler(RoomInternalServerException.class)
    public ResponseEntity<ApiResult<String>> handleRoomInternalServerException(RoomInternalServerException e) {
        return response("room.internalServer", e);
    }

    @ExceptionHandler(RoomNotFoundOwnerException.class)
    public ResponseEntity<ApiResult<String>> handleRoomNotFoundOwnerException(RoomNotFoundOwnerException e) {
        return response("room.notFoundOwner", e);
    }
    
    @ExceptionHandler(RoomNotFoundMemberException.class)
    public ResponseEntity<ApiResult<String>> handleRoomNotFoundMemberException (RoomNotFoundMemberException e) {
        return response("room.notFoundMember", e);
    }

    @ExceptionHandler(RoomAlreadyOwnerException.class)
    public ResponseEntity<ApiResult<String>> handleRoomAlreadyOwnerException (RoomAlreadyOwnerException e) {
        return response("room.alreadyOwner", e);
    }

    @ExceptionHandler(RoomNotOwnerException.class)
    public ResponseEntity<ApiResult<String>> handleRoomNotOwnerException (RoomNotOwnerException e) {
        return response("room.notOwner", e);
    }

    @ExceptionHandler(RoomNotFoundRecommendUserException.class)
    public ResponseEntity<ApiResult<String>> handleRoomNotFoundDelegateUserException(
            RoomNotFoundRecommendUserException e)
    {
        return response("room.notFoundRecommendUser", e);
    }

    @ExceptionHandler(RoomNotParticipantException.class)
    public ResponseEntity<ApiResult<String>> handleRoomNotParticipantException(RoomNotParticipantException e){
        return response("room.notParticipant", e);
    }

}
