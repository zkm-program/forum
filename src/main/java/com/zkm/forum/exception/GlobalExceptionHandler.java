package com.zkm.forum.exception;

import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler({BusinessException.class})
    public BaseResponse<?> businessExceptionHandler(BusinessException e){
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(),e.getMessage());
    }
    @ExceptionHandler({RuntimeException .class})
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e){
        log.error("BusinessException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"系统繁忙，请稍后再试");
    }
}
