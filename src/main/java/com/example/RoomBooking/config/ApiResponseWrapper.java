package com.example.RoomBooking.config;

import com.example.RoomBooking.dto.ApiResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiResponseWrapper implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true; 
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType contentType,
                                  Class converterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if (body instanceof ApiResult) {
            return body;
        }

        ApiResult<?> wrapped = ApiResult.success(body);

        if (body instanceof String) {
            try {
                return objectMapper.writeValueAsString(wrapped);
            } catch (Exception e) {
                throw new RuntimeException("Failed to write ApiResult as JSON string", e);
            }
        }

        return wrapped; 
    }
}
