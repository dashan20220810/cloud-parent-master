package com.qianyi.casinoadmin.config;

import com.qianyi.casinocore.exception.BusinessException;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@ResponseBody
@Slf4j
public class ExceptionAdvice {

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<String> businessException(BusinessException e) {
        return new ResponseEntity<>(e.getMessage());
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("未知异常:", e);
        return new ResponseEntity<>("未知异常");
    }
}
