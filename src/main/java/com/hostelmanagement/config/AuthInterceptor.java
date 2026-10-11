package com.hostelmanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Role-based access control for the REST API.
 *   /api/admin/**   -> ADMIN or WARDEN only (DELETE requests: ADMIN only)
 *   /api/student/** -> STUDENT only
 * The role is stored in the HTTP session when the user logs in.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        HttpSession session = request.getSession(false);
        String role = session == null ? null : (String) session.getAttribute("ROLE");

        if (role == null) {
            return deny(response, 401, "Please login first");
        }
        if (path.startsWith("/api/admin/")) {
            if (role.equals("STUDENT")) {
                return deny(response, 403, "Access denied: admin/warden only");
            }
            if (request.getMethod().equals("DELETE") && !role.equals("ADMIN")) {
                return deny(response, 403, "Only the ADMIN can delete records");
            }
        } else if (path.startsWith("/api/student/")) {
            if (!role.equals("STUDENT")) {
                return deny(response, 403, "Access denied: students only");
            }
        }
        return true;
    }

    private boolean deny(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
        return false;
    }
}
