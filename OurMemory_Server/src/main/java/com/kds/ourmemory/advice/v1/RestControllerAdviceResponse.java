package com.kds.ourmemory.advice.v1;

import com.kds.ourmemory.controller.v1.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.kds.ourmemory.controller.v1.ApiResult.error;

@Slf4j
public class RestControllerAdviceResponse {
    protected ResponseEntity<ApiResult<String>> response(String code, String msg, Exception e) {
        log.warn("================================================================");
        log.warn("resultCode: {}, msg: {}", code, msg);
        log.warn("Exception: {}", e.toString());
        log.warn("================================================================");
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<>(error(code, msg), headers, HttpStatus.OK);
    }
    
    protected ResponseEntity<ApiResult<String>> response(ResultCode resultCode, Exception e) {
        log.warn("================================================================");
        log.warn("resultCode: {}, msg: {}", resultCode.getCode(), resultCode.getMsg());
        log.warn("Exception: {}", e.toString());
        log.warn("================================================================");
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<>(error(resultCode.getCode(), resultCode.getMsg()), headers, HttpStatus.OK);
    }
}
