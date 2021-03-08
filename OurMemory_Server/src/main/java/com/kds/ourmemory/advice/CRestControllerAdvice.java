package com.kds.ourmemory.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.exception.CNotFoundUserException;
import com.kds.ourmemory.advice.exception.CRoomsException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class CRestControllerAdvice {
    
    private ResponseEntity<ErrorResponse> apiResult(HttpStatus status, String msg) {
        ErrorResponse error = new ErrorResponse(status.value(), msg);
        return new ResponseEntity<>(error, status);
    }

	@ExceptionHandler(CNotFoundUserException.class)
	public ResponseEntity<ErrorResponse> handleCNotFoundUserException(CNotFoundUserException e) {
		log.error("HandleCNotFoundUserException" + e);
		return apiResult(HttpStatus.BAD_REQUEST, "Not Found User. cause by: " + e.getMessage());
	}
	
	@ExceptionHandler(CRoomsException.class)
	public ResponseEntity<ErrorResponse> handleCUserAndRoomsException(CRoomsException e) {
	    log.error("CUsersAndRoomsException" + e);
	    return apiResult(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
	}
}
