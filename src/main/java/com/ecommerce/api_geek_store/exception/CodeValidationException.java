package com.ecommerce.api_geek_store.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)

public class CodeValidationException extends RuntimeException {
    public CodeValidationException(String message) {
        super(message);
    }
}
