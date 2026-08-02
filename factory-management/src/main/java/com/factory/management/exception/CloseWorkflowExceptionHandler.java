package com.factory.management.exception;

import com.factory.management.dto.response.ApiResponse;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class CloseWorkflowExceptionHandler {
    @ExceptionHandler(CloseWorkflowException.class)
    public ResponseEntity<ApiResponse<Void>> handle(CloseWorkflowException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(ApiResponse.<Void>builder()
                        .code(exception.getCode())
                        .message(exception.getMessage())
                        .build());
    }
}
