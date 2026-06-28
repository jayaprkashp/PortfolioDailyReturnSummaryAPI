package com.portfolio.dailyreturn.summaryapi.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.portfolio.dailyreturn.summaryapi.exception.ValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,Object> handleValidation(
            ValidationException ex){

        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status",400,
                "error",ex.getMessage());
    }

}
