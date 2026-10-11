package com.hostelmanagement.service;

import com.hostelmanagement.dto.ComplaintRequest;
import com.hostelmanagement.dto.ComplaintUpdate;
import com.hostelmanagement.entity.Complaint;
import com.hostelmanagement.entity.ComplaintStatus;
import com.hostelmanagement.entity.Student;
import com.hostelmanagement.exception.ApiException;
import com.hostelmanagement.repository.ComplaintRepository;
import com.hostelmanagement.util.Check;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepo;

    public ComplaintService(ComplaintRepository complaintRepo) {
        this.complaintRepo = complaintRepo;
    }

    public List<Complaint> listAll() {
        return complaintRepo.findAllByOrderByCreatedAtDesc();
    }

    public List<Complaint> forStudent(Long studentId) {
        return complaintRepo.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Transactional
    public Complaint create(Student student, ComplaintRequest r) {
        Complaint c = new Complaint();
        c.setStudent(student);
        c.setTitle(Check.text(r.title(), "Title"));
        c.setCategory(Check.optional(r.category()) == null ? "Other" : r.category().trim());
        c.setDescription(Check.text(r.description(), "Description"));
        c.setStatus(ComplaintStatus.PENDING);
        c.setCreatedAt(LocalDateTime.now());
        return complaintRepo.save(c);
    }

    @Transactional
    public Complaint updateStatus(Long id, ComplaintUpdate u) {
        Complaint c = complaintRepo.findById(id).orElseThrow(() -> ApiException.notFound("Complaint not found"));
        c.setStatus(Check.enumValue(ComplaintStatus.class, u.status(), "status"));
        c.setAdminRemarks(Check.optional(u.remarks()));
        return complaintRepo.save(c);
    }
}
