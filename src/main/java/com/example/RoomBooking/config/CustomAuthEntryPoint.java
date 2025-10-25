package com.example.RoomBooking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.RoomBooking.dto.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String message = resolveMessage(ex);

        String json = mapper.writeValueAsString(ApiResult.fail(message));
        response.getWriter().write(json);
    }

    private String resolveMessage(AuthenticationException ex) {

        if (ex instanceof BadCredentialsException)
            return "Bad credentials";

        if (ex instanceof LockedException)
            return "Account is locked";

        if (ex instanceof DisabledException)
            return "Account is disabled";

        if (ex instanceof CredentialsExpiredException)
            return "Password expired";

        if (ex instanceof AccountExpiredException)
            return "Account expired";

        return "Unauthorized";
    }
}