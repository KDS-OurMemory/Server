package com.kds.ourmemory.v1.advice.common;

import com.kds.ourmemory.v1.advice.RestControllerAdviceResponse;
import com.kds.ourmemory.v1.advice.common.exception.ColumnEncryptException;
import com.kds.ourmemory.v1.controller.ApiResult;
import org.springframework.context.MessageSource;
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

/**
 * Because the communication was successful, the status code value is set to 200.
 * And the error code value and message are passed.
 */

@Order()   // By setting the priority to the lowest level(default value),
           // each functional advice handles an exception that could not be addressed.
@RestControllerAdvice
public class CommonControllerAdvice extends RestControllerAdviceResponse {

	public CommonControllerAdvice(MessageSource messageSource) {
		super(messageSource);
	}

    /* Http Status Error */
    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
	public ResponseEntity<ApiResult<String>> handleBadRequestException(Exception e) {
	    return response("common.badRequest", e);
	}
	
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ApiResult<String>> handleNoHandlerFoundException(NoHandlerFoundException e) {
	    return response("common.badRequest", e);
	}
	
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ApiResult<String>> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
	    return response("common.unsupportedMediaType", e);
	}

	@ExceptionHandler({Exception.class, RuntimeException.class})
	public ResponseEntity<ApiResult<String>> handleException(Exception e) {
	    return response("common.internalServer", e);
	}

	/* Custom Error */
	@ExceptionHandler(IncorrectResultSizeDataAccessException.class)
	public ResponseEntity<ApiResult<String>> handleDataSizeException(IncorrectResultSizeDataAccessException e) {
		return response("common.incorrectResultSize", e);
	}

	@ExceptionHandler(ColumnEncryptException.class)
	public ResponseEntity<ApiResult<String>> handleColumnEncryptException(ColumnEncryptException e) {
		return response("common.columnEncrypt", e);
	}
}
