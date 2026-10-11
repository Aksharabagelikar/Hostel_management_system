package com.hostelmanagement.repository;

import com.hostelmanagement.entity.Fee;
import com.hostelmanagement.entity.FeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByStudentIdOrderByDueDateDesc(Long studentId);
    List<Fee> findByStatusOrderByDueDateDesc(FeeStatus status);
    List<Fee> findAllByOrderByDueDateDesc();
    long countByStatus(FeeStatus status);
    void deleteByStudentId(Long studentId);

    @Query("select coalesce(sum(f.amount), 0.0) from Fee f where f.status = :status")
    Double sumByStatus(@Param("status") FeeStatus status);
}
