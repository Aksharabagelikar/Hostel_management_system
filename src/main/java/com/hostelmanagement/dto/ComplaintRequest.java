package com.hostelmanagement.dto;

import com.hostelmanagement.entity.*;
import java.time.LocalDate;
import java.util.List;

public record ComplaintRequest(String title, String category, String description) { }
