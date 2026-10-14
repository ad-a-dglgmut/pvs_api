package com.parkeye.pvs.service;

import com.parkeye.pvs.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Resolves who is calling.
 *
 * !! There is no authentication yet. For development the caller is taken from the
 * {@code X-User-Id} header and defaults to user 1 (the seeded demo driver).
 * When real auth (JWT/OAuth) is added, change ONLY this class to read the principal instead.
 */
@Component
public class CurrentUser {

    public long id() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "No request context.");
        }
        HttpServletRequest request = servletAttrs.getRequest();
        String header = request.getHeader("X-User-Id");
        if (header == null || header.isBlank()) return 1L;
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_USER", "X-User-Id must be a number.");
        }
    }
}
