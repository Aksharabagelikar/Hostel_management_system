package com.hostelmanagement.dto;

import com.hostelmanagement.entity.*;
import java.time.LocalDate;
import java.util.List;

public record AttendanceBatch(LocalDate date, List<AttendanceMark> records) { }
