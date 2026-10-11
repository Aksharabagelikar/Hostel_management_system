package com.hostelmanagement.dto;

import com.hostelmanagement.entity.*;
import java.time.LocalDate;
import java.util.List;

public record FeeRequest(Long studentId, Double amount, LocalDate dueDate, String description) { }
