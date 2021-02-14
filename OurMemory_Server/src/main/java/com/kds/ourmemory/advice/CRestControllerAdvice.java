package com.kds.ourmemory.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.exception.CNotFoundUserException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class CRestControllerAdvice {

	@ExceptionHandler(CNotFoundUserException.class)
	public ResponseEntity<ErrorResponse> HandleCNotFoundUserException(CNotFoundUserException e) {
		log.error("HandleCNotFoundUserException" + e);
		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value() ,"Not Found User. cause by: " + e.getMessage());
		return new ResponseEntity<ErrorResponse>(error, HttpStatus.BAD_REQUEST);
	}
}
