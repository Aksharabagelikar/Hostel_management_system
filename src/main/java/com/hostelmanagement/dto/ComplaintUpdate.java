package com.hostelmanagement.dto;

import com.hostelmanagement.entity.*;
import java.time.LocalDate;
import java.util.List;

public record ComplaintUpdate(String status, String remarks) { }
