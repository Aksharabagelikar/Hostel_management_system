package com.hostelmanagement.controller;

import com.hostelmanagement.dto.ComplaintUpdate;
import com.hostelmanagement.entity.Complaint;
import com.hostelmanagement.service.ComplaintService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin/Warden: view complaints and update their status. */
@RestController
@RequestMapping("/api/admin/complaints")
public class ComplaintController {

    private final ComplaintService service;

    public ComplaintController(ComplaintService service) {
        this.service = service;
    }

    @GetMapping
    public List<Complaint> list() {
        return service.listAll();
    }

    @PutMapping("/{id}")
    public Complaint update(@PathVariable Long id, @RequestBody ComplaintUpdate update) {
        return service.updateStatus(id, update);
    }
}
