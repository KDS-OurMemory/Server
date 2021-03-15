package com.kds.ourmemory.advice;

import static com.kds.ourmemory.controller.v1.ApiResult.error;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.exception.CMemorysException;
import com.kds.ourmemory.advice.exception.CRoomsException;
import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.controller.v1.ApiResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class CRestControllerAdvice {
    
    private final MessageSource messageSource;
    
    private ResponseEntity<ApiResult<?>> response(String errorCode, String errorMessage, HttpStatus status) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<>(error(errorCode, errorMessage), headers, status);
    }

	@ExceptionHandler(CUserNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<?> handleCNotFoundUserException(CUserNotFoundException e) {
	    log.warn("Not Found User From Users. plz check userId.\n {}", e.getMessage());
		return response(getMessage("userNotFound.code"), getMessage("userNotFound.msg"), HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(CRoomsException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<?> handleCRoomsException(CRoomsException e) {
	    log.warn(e.getMessage(), e);
	    return response(getMessage("userNotFound.code"), getMessage("userNotFound.msg"), HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(CMemorysException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleCMemorysException(CMemorysException e) {
        log.warn(e.getMessage(), e);
        return response(getMessage("unKnown.code"), getMessage("unKnown.msg"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
	
	@ExceptionHandler({Exception.class, RuntimeException.class})
	public ResponseEntity<?> handleException(Exception e) {
	    log.error("Unexpected exception occurred: {}", e.getMessage(), e);
	    return response(getMessage("unKnown.code"), getMessage("unKnown.msg"), HttpStatus.INTERNAL_SERVER_ERROR); 
	}
	
	private String getMessage(String code) {
	    return getMessage(code, null);
	}
	
	private String getMessage(String code, Object[] args) {
	    return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}
}
