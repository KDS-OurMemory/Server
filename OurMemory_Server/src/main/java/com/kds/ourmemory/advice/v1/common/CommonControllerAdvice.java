package com.kds.ourmemory.advice.v1.common;

import com.kds.ourmemory.advice.v1.RestControllerAdviceResponse;
import com.kds.ourmemory.controller.v1.ApiResult;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
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

import static com.kds.ourmemory.advice.v1.common.CommonResultCode.*;

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
    public ResponseEntity<ApiResult<String>> handleDataSizeException(IncorrectResultSizeDataAccessException e) {
        return response(INCORRECT_RESULT_SIZE, e);
    }

	@ExceptionHandler(FileSizeLimitExceededException.class)
	public ResponseEntity<ApiResult<String>> handleFileSizeLimitExceededException(FileSizeLimitExceededException e) {
		return response(FILE_SIZE_OVERFLOW, e);
	}
    
    
    /* Http Status Error */
    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
	public ResponseEntity<ApiResult<String>> handleBadRequestException(Exception e) {
	    return response(BAD_REQUEST, e);
	}
	
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ApiResult<String>> handleNoHandlerFoundException(NoHandlerFoundException e) {
	    return response(NOT_FOUND, e);
	}
	
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ApiResult<String>> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
	    return response(UNSUPPORTED_MEDIA_TYPE, e);
	}

	@ExceptionHandler({Exception.class, RuntimeException.class})
	public ResponseEntity<ApiResult<String>> handleException(Exception e) {
	    return response(INTERNAL_SERVER_ERROR, new Exception("Unexpected exception occurred: " + e.getMessage(), e)); 
	}
}
