package com.hostelmanagement.repository;

import com.hostelmanagement.entity.Attendance;
import com.hostelmanagement.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByAttendanceDate(LocalDate date);
    List<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);
    Optional<Attendance> findByStudentIdAndAttendanceDate(Long studentId, LocalDate date);
    long countByStudentId(Long studentId);
    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);
    long countByStatus(AttendanceStatus status);
    void deleteByStudentId(Long studentId);
}
