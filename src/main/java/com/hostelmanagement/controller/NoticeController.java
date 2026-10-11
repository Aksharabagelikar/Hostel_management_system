package com.hostelmanagement.controller;

import com.hostelmanagement.dto.NoticeRequest;
import com.hostelmanagement.entity.Notice;
import com.hostelmanagement.service.NoticeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Admin/Warden: notice board management. */
@RestController
@RequestMapping("/api/admin/notices")
public class NoticeController {

    private final NoticeService service;

    public NoticeController(NoticeService service) {
        this.service = service;
    }

    @GetMapping
    public List<Notice> list() {
        return service.listAll();
    }

    @PostMapping
    public Notice create(@RequestBody NoticeRequest request, HttpSession session) {
        return service.create(request, (String) session.getAttribute("FULL_NAME"));
    }

    @PutMapping("/{id}")
    public Notice update(@PathVariable Long id, @RequestBody NoticeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        service.delete(id);
        return Map.of("message", "Notice deleted");
    }
}
