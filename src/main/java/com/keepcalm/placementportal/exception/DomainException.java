package com.keepcalm.placementportal.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DomainException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public DomainException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static DomainException notFound(String message) {
        return new DomainException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }

    public static DomainException forbidden(String message) {
        return new DomainException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }

    public static DomainException conflict(String code, String message) {
        return new DomainException(HttpStatus.CONFLICT, code, message);
    }

    public static DomainException unprocessable(String code, String message) {
        return new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }
}
