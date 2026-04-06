package com.banksoft.useradmin.common;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class CbsException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    private CbsException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public static CbsException notFound(String message) {
        return new CbsException(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }

    public static CbsException conflict(String message) {
        return new CbsException(HttpStatus.CONFLICT, "CONFLICT", message);
    }

    public static CbsException badRequest(String message) {
        return new CbsException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }

    public static CbsException unprocessable(String message) {
        return new CbsException(HttpStatus.UNPROCESSABLE_ENTITY, "UNPROCESSABLE", message);
    }

    public static CbsException forbidden(String message) {
        return new CbsException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }

    public static CbsException unauthorized(String message) {
        return new CbsException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
    }
}
