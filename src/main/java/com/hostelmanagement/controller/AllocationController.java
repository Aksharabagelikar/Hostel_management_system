package com.hostelmanagement.controller;

import com.hostelmanagement.dto.AllocationRequest;
import com.hostelmanagement.entity.RoomAllocation;
import com.hostelmanagement.service.AllocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Admin/Warden: allocate a room, change a room, vacate a room. */
@RestController
@RequestMapping("/api/admin/allocations")
public class AllocationController {

    private final AllocationService service;

    public AllocationController(AllocationService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoomAllocation> active() {
        return service.activeAllocations();
    }

    @PostMapping
    public RoomAllocation allocate(@RequestBody AllocationRequest request) {
        return service.allocate(request.studentId(), request.roomId());
    }

    @PostMapping("/change")
    public RoomAllocation change(@RequestBody AllocationRequest request) {
        return service.changeRoom(request.studentId(), request.roomId());
    }

    @PutMapping("/{id}/vacate")
    public Map<String, String> vacate(@PathVariable Long id) {
        service.vacate(id);
        return Map.of("message", "Room vacated");
    }
}
