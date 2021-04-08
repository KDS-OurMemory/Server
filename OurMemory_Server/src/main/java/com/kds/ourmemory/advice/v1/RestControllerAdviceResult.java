package com.kds.ourmemory.advice.v1;

import static com.kds.ourmemory.controller.v1.ApiResult.error;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.kds.ourmemory.controller.v1.ApiResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestControllerAdviceResult {
    protected ResponseEntity<ApiResult<?>> response(ResultCode resultCode, Exception e) {
        log.warn(e.getMessage());
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<>(error(resultCode.getCode(), resultCode.getMsg()), headers, HttpStatus.OK);
    }
}
