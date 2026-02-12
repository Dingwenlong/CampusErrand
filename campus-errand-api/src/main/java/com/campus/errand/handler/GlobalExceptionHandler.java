package com.campus.errand.handler;

import com.campus.errand.common.Result;
import com.campus.errand.common.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "参数错误")
                .orElse("参数错误");
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        if (message.isEmpty()) {
            message = ResultCode.PARAM_ERROR.getMessage();
        }
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            message = ResultCode.ERROR.getMessage();
        }
        return Result.error(ResultCode.ERROR.getCode(), message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return Result.error(ResultCode.SYSTEM_ERROR);
    }
}
