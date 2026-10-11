package com.hostelmanagement.controller;

import com.hostelmanagement.dto.LoginRequest;
import com.hostelmanagement.dto.LoginResponse;
import com.hostelmanagement.entity.User;
import com.hostelmanagement.exception.ApiException;
import com.hostelmanagement.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Login / logout / current user. Uses the HTTP session (JSESSIONID cookie). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req, HttpServletRequest request) {
        User user = authService.authenticate(req.username(), req.password());

        // start a fresh session after login (prevents session fixation)
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("USER_ID", user.getId());
        session.setAttribute("ROLE", user.getRole().name());
        session.setAttribute("FULL_NAME", user.getFullName());
        session.setAttribute("USERNAME", user.getUsername());
        return toResponse(session);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "Logged out");
    }

    @GetMapping("/me")
    public LoginResponse me(HttpSession session) {
        if (session.getAttribute("ROLE") == null) {
            throw ApiException.unauthorized("Not logged in");
        }
        return toResponse(session);
    }

    private LoginResponse toResponse(HttpSession s) {
        String role = (String) s.getAttribute("ROLE");
        return new LoginResponse((Long) s.getAttribute("USER_ID"), (String) s.getAttribute("USERNAME"),
                (String) s.getAttribute("FULL_NAME"), role, role.equals("STUDENT") ? "/student" : "/admin");
    }
}
