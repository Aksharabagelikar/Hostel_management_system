package com.hostelmanagement.controller;

import com.hostelmanagement.dto.StudentDetails;
import com.hostelmanagement.dto.StudentRequest;
import com.hostelmanagement.entity.Student;
import com.hostelmanagement.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Admin/Warden: student CRUD + search. */
@RestController
@RequestMapping("/api/admin/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Student> list(@RequestParam(required = false) String q) {
        return service.search(q);
    }

    @GetMapping("/{id}")
    public StudentDetails details(@PathVariable Long id) {
        return service.details(id);
    }

    @PostMapping
    public Student create(@RequestBody StudentRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @RequestBody StudentRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        service.delete(id);
        return Map.of("message", "Student deleted");
    }
}
