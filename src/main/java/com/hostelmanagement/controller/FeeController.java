package com.hostelmanagement.controller;

import com.hostelmanagement.dto.FeeRequest;
import com.hostelmanagement.dto.PayRequest;
import com.hostelmanagement.entity.Fee;
import com.hostelmanagement.service.FeeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Admin/Warden: fee management. status = ALL | PENDING | PAID */
@RestController
@RequestMapping("/api/admin/fees")
public class FeeController {

    private final FeeService service;

    public FeeController(FeeService service) {
        this.service = service;
    }

    @GetMapping
    public List<Fee> list(@RequestParam(required = false) String status) {
        return service.list(status);
    }

    @PostMapping
    public Fee create(@RequestBody FeeRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}/pay")
    public Fee pay(@PathVariable Long id, @RequestBody PayRequest request) {
        return service.markPaid(id, request.paymentMode());
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        service.delete(id);
        return Map.of("message", "Fee record deleted");
    }
}
