package com.hostelmanagement.repository;

import com.hostelmanagement.entity.Complaint;
import com.hostelmanagement.entity.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findAllByOrderByCreatedAtDesc();
    List<Complaint> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    long countByStatus(ComplaintStatus status);
    long countByStudentIdAndStatusNot(Long studentId, ComplaintStatus status);
    void deleteByStudentId(Long studentId);
}
