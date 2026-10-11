package com.hostelmanagement.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Serves the two dashboard pages. They live in src/main/resources/protected (NOT in static),
 * so nobody can open them without logging in with the right role:
 *   /admin   -> ADMIN or WARDEN
 *   /student -> STUDENT
 * Anyone else is redirected (to the login page or to their own dashboard).
 */
@RestController
public class PageController {

    @GetMapping("/")
    public ResponseEntity<Resource> home(HttpSession session) {
        String role = (String) session.getAttribute("ROLE");
        if (role == null) {
            return redirect("/login.html");
        }
        return redirect(role.equals("STUDENT") ? "/student" : "/admin");
    }

    @GetMapping("/admin")
    public ResponseEntity<Resource> adminPage(HttpSession session) {
        return serve(session, true);
    }

    @GetMapping("/student")
    public ResponseEntity<Resource> studentPage(HttpSession session) {
        return serve(session, false);
    }

    private ResponseEntity<Resource> serve(HttpSession session, boolean adminPage) {
        String role = (String) session.getAttribute("ROLE");
        if (role == null) {
            return redirect("/login.html");
        }
        boolean isStudent = role.equals("STUDENT");
        if (adminPage == isStudent) {
            // wrong dashboard for this role -> send them to their own
            return redirect(isStudent ? "/student" : "/admin");
        }
        String file = adminPage ? "protected/admin.html" : "protected/student.html";
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header("Cache-Control", "no-store")
                .body(new ClassPathResource(file));
    }

    private ResponseEntity<Resource> redirect(String url) {
        return ResponseEntity.status(302).location(URI.create(url)).build();
    }
}
