package com.kds.ourmemory.advice.v1.common;

import static com.kds.ourmemory.advice.v1.common.CommonResultCode.*;

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

import com.kds.ourmemory.advice.v1.RestControllerAdviceResponse;

/**
 * Because the communication was successful, the status code value is set to 200.
 * And the error code value and message are passed.
 */

@Order(Ordered.LOWEST_PRECEDENCE)   // By setting the priority to the lowest level,
                                    // each functional advice handles an exception that could not be addressed.
@RestControllerAdvice
public class CommonControllerAdvice extends RestControllerAdviceResponse{
	
    /* Custom Error */
    @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
    public ResponseEntity<?> handleDataSizeException(IncorrectResultSizeDataAccessException e) {
        return response(INCORRECT_RESULT_SIZE, e);
    }
    
    
    /* Http Status Error */
    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
	public ResponseEntity<?> handleBadRequestException(Exception e) {
	    return response(BAD_REQUEST, e);
	}
	
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
	    return response(NOT_FOUND, e);
	}
	
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<?> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
	    return response(UNSUPPORTED_MEDIA_TYPE, e);
	}
	
	@ExceptionHandler({Exception.class, RuntimeException.class})
	public ResponseEntity<?> handleException(Exception e) {
	    return response(INTERNAL_SERVER_ERROR, new Exception("Unexpected exception occurred: " + e.getMessage(), e)); 
	}
}
