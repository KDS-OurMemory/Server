package com.kds.ourmemory.advice;

import static com.kds.ourmemory.controller.v1.ApiResult.error;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.exception.CMemoryException;
import com.kds.ourmemory.advice.exception.CRoomException;
import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.advice.exception.CUserException;
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
	    log.warn(e.getMessage());
		return response(getMessage("user.notFound.code"), e.getMessage(), HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(CUserException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleCUsersException(CUserException e) {
        log.warn(e.getMessage(), e);
        return response(getMessage("unKnown.code"), e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
	
	@ExceptionHandler(CRoomException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<?> handleCRoomsException(CRoomException e) {
	    log.warn(e.getMessage(), e);
	    return response(getMessage("unKnown.code"), e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(CMemoryException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleCMemorysException(CMemoryException e) {
        log.warn(e.getMessage(), e);
        return response(getMessage("unKnown.code"), e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
	
	@ExceptionHandler(MissingServletRequestParameterException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<?> handleBadParameterException(MissingServletRequestParameterException e) {
	    log.warn(e.getMessage());
	    return response(getMessage("queryString.badParameter.code"), e.getMessage(), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(IncorrectResultSizeDataAccessException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<?> handleDataSizeException(IncorrectResultSizeDataAccessException e) {
	    log.warn(e.getMessage());
	    return response(getMessage("DB.incorrectResultSize.code"), e.getMessage(), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler({Exception.class, RuntimeException.class})
	public ResponseEntity<?> handleException(Exception e) {
	    log.error("Unexpected exception occurred: {}", e.getMessage(), e);
	    return response(getMessage("unKnown.code"), e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
	}
	
	private String getMessage(String code) {
	    return getMessage(code, null);
	}
	
	private String getMessage(String code, Object[] args) {
	    return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}
}
