package com.hostelmanagement.dto;

import com.hostelmanagement.entity.*;
import java.time.LocalDate;
import java.util.List;

public record StudentDetails(Student student, RoomAllocation allocation, List<Fee> fees, List<Complaint> complaints, AttendanceSummary attendance) { }
