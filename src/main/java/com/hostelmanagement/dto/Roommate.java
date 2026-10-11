package com.hostelmanagement.dto;

import com.hostelmanagement.entity.*;
import java.time.LocalDate;
import java.util.List;

public record Roommate(String name, String rollNo, String course, String phone, String email) { }
