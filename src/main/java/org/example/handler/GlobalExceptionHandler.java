package org.example.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e) {
        log.error("[全局异常] 异常类型:{} | 异常信息:{}", e.getClass().getSimpleName(), e.getMessage(), e);
        return "抱歉，服务器出现异常，请稍后再试。";
    }
}
