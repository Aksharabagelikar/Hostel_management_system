package com.hostelmanagement.controller;

import com.hostelmanagement.dto.AttendanceBatch;
import com.hostelmanagement.dto.AttendanceRow;
import com.hostelmanagement.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Admin/Warden: mark and view attendance for a date. */
@RestController
@RequestMapping("/api/admin/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<AttendanceRow> forDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.forDate(date == null ? LocalDate.now() : date);
    }

    @PostMapping
    public Map<String, Object> save(@RequestBody AttendanceBatch batch) {
        int saved = service.save(batch);
        return Map.of("message", "Attendance saved for " + saved + " student(s)", "saved", saved);
    }
}
