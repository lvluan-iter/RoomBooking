package com.example.RoomBooking.config;

import com.example.RoomBooking.dto.ApiResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

public class ApiResponseWrapper implements ResponseBodyAdvice<Object> {
    
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        String converterName = converterType.getSimpleName();
        return !"StringHttpMessageConverter".equals(converterName);
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
        
        return ApiResult.success(body);
    }
}
