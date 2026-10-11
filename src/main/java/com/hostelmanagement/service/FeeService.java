package com.hostelmanagement.service;

import com.hostelmanagement.dto.FeeRequest;
import com.hostelmanagement.entity.Fee;
import com.hostelmanagement.entity.FeeStatus;
import com.hostelmanagement.exception.ApiException;
import com.hostelmanagement.repository.FeeRepository;
import com.hostelmanagement.repository.StudentRepository;
import com.hostelmanagement.util.Check;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FeeService {

    private final FeeRepository feeRepo;
    private final StudentRepository studentRepo;

    public FeeService(FeeRepository feeRepo, StudentRepository studentRepo) {
        this.feeRepo = feeRepo;
        this.studentRepo = studentRepo;
    }

    /** status: PENDING | PAID | null (all) */
    public List<Fee> list(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("all")) {
            return feeRepo.findAllByOrderByDueDateDesc();
        }
        return feeRepo.findByStatusOrderByDueDateDesc(Check.enumValue(FeeStatus.class, status, "status"));
    }

    public List<Fee> forStudent(Long studentId) {
        return feeRepo.findByStudentIdOrderByDueDateDesc(studentId);
    }

    @Transactional
    public Fee create(FeeRequest r) {
        if (r.studentId() == null) throw ApiException.badRequest("Student is required");
        if (r.amount() == null || r.amount() <= 0) throw ApiException.badRequest("Amount must be greater than 0");
        if (r.dueDate() == null) throw ApiException.badRequest("Due date is required");
        Fee f = new Fee();
        f.setStudent(studentRepo.findById(r.studentId()).orElseThrow(() -> ApiException.notFound("Student not found")));
        f.setAmount(r.amount());
        f.setDueDate(r.dueDate());
        f.setDescription(Check.text(r.description(), "Description"));
        f.setStatus(FeeStatus.PENDING);
        return feeRepo.save(f);
    }

    @Transactional
    public Fee markPaid(Long id, String paymentMode) {
        Fee f = feeRepo.findById(id).orElseThrow(() -> ApiException.notFound("Fee record not found"));
        if (f.getStatus() == FeeStatus.PAID) {
            throw ApiException.badRequest("This fee is already paid");
        }
        f.setStatus(FeeStatus.PAID);
        f.setPaidDate(LocalDate.now());
        f.setPaymentMode(Check.optional(paymentMode) == null ? "Cash" : paymentMode.trim());
        return feeRepo.save(f);
    }

    @Transactional
    public void delete(Long id) {
        if (!feeRepo.existsById(id)) throw ApiException.notFound("Fee record not found");
        feeRepo.deleteById(id);
    }
}
