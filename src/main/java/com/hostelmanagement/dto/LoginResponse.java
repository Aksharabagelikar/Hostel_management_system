package com.hostelmanagement.dto;

import com.hostelmanagement.entity.*;
import java.time.LocalDate;
import java.util.List;

public record LoginResponse(Long userId, String username, String fullName, String role, String redirect) { }
