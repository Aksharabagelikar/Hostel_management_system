package com.hostelmanagement.controller;

import com.hostelmanagement.dto.RoomRequest;
import com.hostelmanagement.dto.RoomView;
import com.hostelmanagement.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Admin/Warden: room CRUD. filter = all | available | occupied | vacant */
@RestController
@RequestMapping("/api/admin/rooms")
public class RoomController {

    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoomView> list(@RequestParam(required = false) String filter) {
        return service.list(filter);
    }

    @PostMapping
    public RoomView create(@RequestBody RoomRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public RoomView update(@PathVariable Long id, @RequestBody RoomRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        service.delete(id);
        return Map.of("message", "Room deleted");
    }
}
