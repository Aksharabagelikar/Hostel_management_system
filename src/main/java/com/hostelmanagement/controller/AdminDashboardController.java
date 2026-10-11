package com.hostelmanagement.controller;

import com.hostelmanagement.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final ReportService reportService;

    public AdminDashboardController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return reportService.adminDashboard();
    }

    @GetMapping("/reports")
    public Map<String, Object> reports() {
        return reportService.reports();
    }
}
