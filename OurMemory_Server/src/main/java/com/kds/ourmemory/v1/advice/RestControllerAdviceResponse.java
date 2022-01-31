package com.kds.ourmemory.v1.advice;

import com.kds.ourmemory.v1.controller.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.kds.ourmemory.v1.controller.ApiResult.error;

@Slf4j
@RequiredArgsConstructor
public class RestControllerAdviceResponse {

    private final MessageSource messageSource;

    protected ResponseEntity<ApiResult<String>> response(String resultCode, ArgsRuntimeException e) {
        return response(
                getMessage(resultCode + ".code"),
                getMessage(resultCode + ".resultMessage"),
                getMessage(resultCode + ".detailMessage", e.getArgs())
        );
    }

    protected ResponseEntity<ApiResult<String>> response(String resultCode, Exception e) {
        return response(
                getMessage(resultCode + ".code"),
                getMessage(resultCode + ".resultMessage"),
                getMessage(resultCode + ".detailMessage", e.getMessage())
        );
    }

    private ResponseEntity<ApiResult<String>> response(String code, String resultMessage, String detailMessage) {
        log.error("================================================================");
        log.error("resultCode: {}", code);
        log.error("resultMessage: {}", resultMessage);
        log.error("detailMessage: {}", detailMessage);
        log.error("================================================================");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<>(error(code, resultMessage, detailMessage), headers, HttpStatus.OK);
    }

    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

}
