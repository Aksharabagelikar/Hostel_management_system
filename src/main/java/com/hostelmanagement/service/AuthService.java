package com.hostelmanagement.service;

import com.hostelmanagement.entity.User;
import com.hostelmanagement.exception.ApiException;
import com.hostelmanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    /** Checks username + password (BCrypt) and returns the user, or throws 401. */
    public User authenticate(String username, String password) {
        if (username == null || password == null) {
            throw ApiException.unauthorized("Invalid username or password");
        }
        User user = userRepo.findByUsername(username.trim())
                .orElseThrow(() -> ApiException.unauthorized("Invalid username or password"));
        if (!encoder.matches(password, user.getPassword())) {
            throw ApiException.unauthorized("Invalid username or password");
        }
        return user;
    }
}
