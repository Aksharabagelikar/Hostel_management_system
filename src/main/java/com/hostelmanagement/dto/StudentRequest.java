package com.hostelmanagement.dto;

import com.hostelmanagement.entity.*;
import java.time.LocalDate;
import java.util.List;

public record StudentRequest(String name, String rollNo, String email, String phone, String course, Integer yearOfStudy, String guardianName, String guardianPhone, String address, String username, String password) { }
