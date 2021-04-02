package com.kds.ourmemory.advice.v1.common;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResult;

import lombok.extern.slf4j.Slf4j;

/**
 * Because the communication was successful, the status code value is set to 200 
 * and the error code value and message are passed.
 * 
 * @author idean
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class CommonControllerAdvice extends RestControllerAdviceResult{
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<?> handleBadParameterException(MissingServletRequestParameterException e) {
	    log.warn(e.getMessage());
	    return response(CommonResultCode.BAD_PARAMETER);
	}
	
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
	    log.warn(e.getMessage());
	    return response(CommonResultCode.NOT_FOUND);
	}
	
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<?> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
	    log.warn(e.getMessage());
	    return response(CommonResultCode.UNSUPPORTED_MEDIA_TYPE);
	}
	
	@ExceptionHandler(IncorrectResultSizeDataAccessException.class)
    public ResponseEntity<?> handleDataSizeException(IncorrectResultSizeDataAccessException e) {
        log.warn(e.getMessage());
        return response(CommonResultCode.INCORRECT_RESULT_SIZE);
    }
	
	@ExceptionHandler({Exception.class, RuntimeException.class})
	public ResponseEntity<?> handleException(Exception e) {
	    log.error("Unexpected exception occurred: {}", e.getMessage(), e);
	    return response(CommonResultCode.INTERNAL_SERVER_ERROR); 
	}
}
