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
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kds.ourmemory.advice.exception.CMemoryException;
import com.kds.ourmemory.advice.exception.CRoomException;
import com.kds.ourmemory.advice.exception.CUserException;
import com.kds.ourmemory.advice.exception.CUserNotFoundException;
import com.kds.ourmemory.advice.exception.CUserPatchTokenException;
import com.kds.ourmemory.controller.v1.ApiResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API 예외처리 어드바이스 코드
 * 
 * 해당 코드로 캐치된 경우, 통신 자체는 성공한 것이기 때문에 상태코드 값을 200으로 하고
 * 에러코드 값을 전달하여 프론트에서 분기하도록 한다.
 * 
 * @author idean
 *
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class CRestControllerAdvice {
    
    private final MessageSource messageSource;
    
    private ResponseEntity<ApiResult<?>> response(String errorCode, String errorMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<>(error(errorCode, errorMessage), headers, HttpStatus.OK);
    }

	@ExceptionHandler(CUserNotFoundException.class)
	public ResponseEntity<?> handleCNotFoundUserException(CUserNotFoundException e) {
	    log.warn(e.getMessage());
		return response(getMessage("user.notFound.code"), getMessage("user.notFound.msg"));
	}
	
	@ExceptionHandler(CUserPatchTokenException.class)
	public ResponseEntity<?> handleCUserPatchTokenException(CUserPatchTokenException e) {
	    log.warn(e.getMessage());
	    return response(getMessage("user.patch.failTokenUpdate.code"), getMessage("user.patch.failTokenUpdate.msg"));
	}
	
	@ExceptionHandler(CUserException.class)
    public ResponseEntity<?> handleCUsersException(CUserException e) {
        log.warn(e.getMessage(), e);
        return response(getMessage("unKnown.code"), getMessage("unKnown.msg"));
    }
	
	@ExceptionHandler(CRoomException.class)
	public ResponseEntity<?> handleCRoomsException(CRoomException e) {
	    log.warn(e.getMessage(), e);
	    return response(getMessage("unKnown.code"), getMessage("unKnown.msg"));
	}
	
	@ExceptionHandler(CMemoryException.class)
    public ResponseEntity<?> handleCMemorysException(CMemoryException e) {
        log.warn(e.getMessage(), e);
        return response(getMessage("unKnown.code"), getMessage("unKnown.msg"));
    }
	
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<?> handleBadParameterException(MissingServletRequestParameterException e) {
	    log.warn(e.getMessage());
	    return response(getMessage("queryString.badParameter.code"), getMessage("queryString.badParameter.msg"));
	}
	
	@ExceptionHandler(IncorrectResultSizeDataAccessException.class)
	public ResponseEntity<?> handleDataSizeException(IncorrectResultSizeDataAccessException e) {
	    log.warn(e.getMessage());
	    return response(getMessage("DB.incorrectResultSize.code"), getMessage("DB.incorrectResultSize.msg"));
	}
	
	@ExceptionHandler({Exception.class, RuntimeException.class})
	public ResponseEntity<?> handleException(Exception e) {
	    log.error("Unexpected exception occurred: {}", e.getMessage(), e);
	    return response(getMessage("unKnown.code"), getMessage("unKnown.msg")); 
	}
	
	private String getMessage(String code) {
	    return getMessage(code, null);
	}
	
	private String getMessage(String code, Object[] args) {
	    return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
	}
}
