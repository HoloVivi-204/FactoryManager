package com.factory.management.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CloseWorkflowException extends RuntimeException {
    private final int code;
    private final HttpStatus status;

    public CloseWorkflowException(int code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public static CloseWorkflowException badRequest(int code, String message) {
        return new CloseWorkflowException(code, message, HttpStatus.BAD_REQUEST);
    }

    public static CloseWorkflowException notFound(int code, String message) {
        return new CloseWorkflowException(code, message, HttpStatus.NOT_FOUND);
    }

    public static CloseWorkflowException conflict(int code, String message) {
        return new CloseWorkflowException(code, message, HttpStatus.CONFLICT);
    }

    public static CloseWorkflowException unprocessable(int code, String message) {
        return new CloseWorkflowException(code, message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
