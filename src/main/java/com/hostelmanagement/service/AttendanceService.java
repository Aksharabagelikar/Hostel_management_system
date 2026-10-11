package com.hostelmanagement.service;

import com.hostelmanagement.dto.AttendanceBatch;
import com.hostelmanagement.dto.AttendanceMark;
import com.hostelmanagement.dto.AttendanceRow;
import com.hostelmanagement.dto.AttendanceSummary;
import com.hostelmanagement.entity.Attendance;
import com.hostelmanagement.entity.AttendanceStatus;
import com.hostelmanagement.entity.Student;
import com.hostelmanagement.exception.ApiException;
import com.hostelmanagement.repository.AttendanceRepository;
import com.hostelmanagement.repository.StudentRepository;
import com.hostelmanagement.util.Check;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepo;
    private final StudentRepository studentRepo;

    public AttendanceService(AttendanceRepository attendanceRepo, StudentRepository studentRepo) {
        this.attendanceRepo = attendanceRepo;
        this.studentRepo = studentRepo;
    }

    /** One row per student for the chosen date (status is null when not marked yet). */
    public List<AttendanceRow> forDate(LocalDate date) {
        Map<Long, AttendanceStatus> marked = new HashMap<>();
        for (Attendance a : attendanceRepo.findByAttendanceDate(date)) {
            marked.put(a.getStudent().getId(), a.getStatus());
        }
        return studentRepo.findAll(Sort.by("name")).stream()
                .map(s -> new AttendanceRow(s.getId(), s.getName(), s.getRollNo(),
                        marked.containsKey(s.getId()) ? marked.get(s.getId()).name() : null))
                .toList();
    }

    @Transactional
    public int save(AttendanceBatch batch) {
        if (batch == null || batch.date() == null) {
            throw ApiException.badRequest("Date is required");
        }
        if (batch.date().isAfter(LocalDate.now())) {
            throw ApiException.badRequest("Cannot mark attendance for a future date");
        }
        if (batch.records() == null) {
            return 0;
        }
        int saved = 0;
        for (AttendanceMark mark : batch.records()) {
            if (mark.status() == null || mark.status().isBlank()) {
                continue;
            }
            AttendanceStatus status = Check.enumValue(AttendanceStatus.class, mark.status(), "status");
            Student student = studentRepo.findById(mark.studentId())
                    .orElseThrow(() -> ApiException.notFound("Student not found"));
            Attendance a = attendanceRepo.findByStudentIdAndAttendanceDate(student.getId(), batch.date())
                    .orElseGet(Attendance::new);
            a.setStudent(student);
            a.setAttendanceDate(batch.date());
            a.setStatus(status);
            attendanceRepo.save(a);
            saved++;
        }
        return saved;
    }

    public List<Attendance> history(Long studentId) {
        return attendanceRepo.findByStudentIdOrderByAttendanceDateDesc(studentId);
    }

    public AttendanceSummary summary(Long studentId) {
        long total = attendanceRepo.countByStudentId(studentId);
        long present = attendanceRepo.countByStudentIdAndStatus(studentId, AttendanceStatus.PRESENT);
        long absent = attendanceRepo.countByStudentIdAndStatus(studentId, AttendanceStatus.ABSENT);
        long leave = attendanceRepo.countByStudentIdAndStatus(studentId, AttendanceStatus.LEAVE);
        return new AttendanceSummary(total, present, absent, leave, percent(present, total));
    }

    public double overallPercent() {
        long total = attendanceRepo.count();
        long present = attendanceRepo.countByStatus(AttendanceStatus.PRESENT);
        return percent(present, total);
    }

    private double percent(long part, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(part * 1000.0 / total) / 10.0;
    }
}
