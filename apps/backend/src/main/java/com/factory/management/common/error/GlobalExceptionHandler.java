package com.factory.management.common.error;

import com.factory.management.common.config.RequestCorrelationFilter;
import com.factory.management.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<Void>> handlingException(Exception exception, HttpServletRequest request) {
        log.error("Exception: ", exception);
        return response(ErrorCode.UNCATEGORIZED_EXCEPTION, request, null);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Void>> handlingAppException(AppException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        return response(errorCode, request, null);
    }

//    @ExceptionHandler(value = AccessDeniedException.class)
//    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
//        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
//
//        return ResponseEntity.status(errorCode.getStatusCode())
//                .body(ApiResponse.builder()
//                        .code(errorCode.getCode())
//                        .message(errorCode.getMessage())
//                        .build());
//    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handlingValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        if (exception.getFieldError() != null) {
            try {
                errorCode = ErrorCode.valueOf(exception.getFieldError().getDefaultMessage());
            } catch (IllegalArgumentException ignored) {
                log.warn("Unknown validation error key: {}", exception.getFieldError().getDefaultMessage());
            }
        }

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), resolveValidationMessage(error.getDefaultMessage())));
        return response(errorCode, request, fieldErrors);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
    ResponseEntity<ApiResponse<Void>> handlingConflict(Exception exception, HttpServletRequest request) {
        log.warn("Data conflict: {}", exception.getMessage());
        ApiResponse<Void> body = base(ErrorCode.INVALID_KEY, request)
                .message("Dữ liệu đã thay đổi hoặc bị trùng. Vui lòng tải lại và thử lại.")
                .build();
        return ResponseEntity.status(409).body(body);
    }

    private ResponseEntity<ApiResponse<Void>> response(
            ErrorCode errorCode,
            HttpServletRequest request,
            Map<String, String> fieldErrors
    ) {
        ApiResponse<Void> body = base(errorCode, request).fieldErrors(fieldErrors).build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(body);
    }

    private ApiResponse.ApiResponseBuilder<Void> base(ErrorCode errorCode, HttpServletRequest request) {
        return ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .requestId(MDC.get(RequestCorrelationFilter.REQUEST_ID_MDC_KEY));
    }

    private String resolveValidationMessage(String key) {
        if (key == null) return ErrorCode.INVALID_KEY.getMessage();
        try {
            return ErrorCode.valueOf(key).getMessage();
        } catch (IllegalArgumentException ignored) {
            return key;
        }
    }
}
