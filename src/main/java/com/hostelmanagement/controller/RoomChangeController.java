package com.hostelmanagement.controller;

import com.hostelmanagement.dto.RoomChangeDecision;
import com.hostelmanagement.entity.RoomChangeRequest;
import com.hostelmanagement.service.RoomChangeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin/Warden: process room change requests raised by students. */
@RestController
@RequestMapping("/api/admin/room-requests")
public class RoomChangeController {

    private final RoomChangeService service;

    public RoomChangeController(RoomChangeService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoomChangeRequest> list() {
        return service.listAll();
    }

    @PutMapping("/{id}/approve")
    public RoomChangeRequest approve(@PathVariable Long id, @RequestBody RoomChangeDecision decision) {
        return service.approve(id, decision);
    }

    @PutMapping("/{id}/reject")
    public RoomChangeRequest reject(@PathVariable Long id, @RequestBody RoomChangeDecision decision) {
        return service.reject(id, decision);
    }
}
