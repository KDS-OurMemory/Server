package com.kds.ourmemory.advice.v1.common;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResult;

/**
 * Because the communication was successful, the status code value is set to 200 
 * and the error code value and message are passed.
 * 
 * @author idean
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class CommonControllerAdvice extends RestControllerAdviceResult{
	
    /* Custom Error */
    @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
    public ResponseEntity<?> handleDataSizeException(IncorrectResultSizeDataAccessException e) {
        return response(CommonResultCode.INCORRECT_RESULT_SIZE, e);
    }
    
    
    /* Http Status Error */
    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
	public ResponseEntity<?> handleBadParameterException(Exception e) {
	    return response(CommonResultCode.BAD_PARAMETER, e);
	}
	
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
	    return response(CommonResultCode.NOT_FOUND, e);
	}
	
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<?> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
	    return response(CommonResultCode.UNSUPPORTED_MEDIA_TYPE, e);
	}
	
	@ExceptionHandler({Exception.class, RuntimeException.class})
	public ResponseEntity<?> handleException(Exception e) {
	    return response(CommonResultCode.INTERNAL_SERVER_ERROR, new Exception("Unexpected exception occurred: " + e.getMessage(), e)); 
	}
}