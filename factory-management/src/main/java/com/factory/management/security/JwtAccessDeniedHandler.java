package com.factory.management.security;

import com.factory.management.dto.response.ApiResponse;
import com.factory.management.config.RequestCorrelationFilter;
import com.factory.management.exception.ErrorCode;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import org.slf4j.MDC;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        response.setStatus(errorCode.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.builder()
                .code(errorCode.getCode()).message(errorCode.getMessage())
                .timestamp(LocalDateTime.now()).path(request.getRequestURI())
                .requestId(MDC.get(RequestCorrelationFilter.REQUEST_ID_MDC_KEY)).build()));
    }
}
