package ru.tsu.hits.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import ru.tsu.hits.userservice.exception.*;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ExceptionHandlerFilter extends GenericFilterBean {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            try {
                filterChain.doFilter(servletRequest, servletResponse);
            } catch (JwtTokenMissingException | JwtTokenMalformedException | JwtTokenExpiredException ex) {
                handleException(request, (HttpServletResponse) servletResponse, ex);
            } catch (Exception ex) {
                handleException(request, (HttpServletResponse) servletResponse, new RuntimeException("Internal Server Error"));
            }
        }
    }

    private void handleException(HttpServletRequest request, HttpServletResponse response, RuntimeException ex) throws IOException {
        response.setStatus(getHttpStatus(ex).value());
        response.setContentType("application/json");

        String json = objectMapper.writeValueAsString(
                new ErrorResponse(ex.getMessage())
        );

        response.getWriter().write(json);
    }

    private HttpStatus getHttpStatus(RuntimeException ex) {
        if (ex instanceof JwtTokenExpiredException) {
            return HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof JwtTokenMissingException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof JwtTokenMalformedException) {
            return HttpStatus.BAD_REQUEST;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}
