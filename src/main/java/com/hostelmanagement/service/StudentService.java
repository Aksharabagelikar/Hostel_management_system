package com.hostelmanagement.service;

import com.hostelmanagement.dto.StudentDetails;
import com.hostelmanagement.dto.StudentRequest;
import com.hostelmanagement.entity.Role;
import com.hostelmanagement.entity.RoomAllocation;
import com.hostelmanagement.entity.Student;
import com.hostelmanagement.entity.User;
import com.hostelmanagement.exception.ApiException;
import com.hostelmanagement.repository.*;
import com.hostelmanagement.util.Check;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final RoomAllocationRepository allocationRepo;
    private final FeeRepository feeRepo;
    private final ComplaintRepository complaintRepo;
    private final AttendanceRepository attendanceRepo;
    private final RoomChangeRequestRepository requestRepo;
    private final AttendanceService attendanceService;

    public StudentService(StudentRepository studentRepo, UserRepository userRepo, PasswordEncoder encoder,
                          RoomAllocationRepository allocationRepo, FeeRepository feeRepo,
                          ComplaintRepository complaintRepo, AttendanceRepository attendanceRepo,
                          RoomChangeRequestRepository requestRepo, AttendanceService attendanceService) {
        this.studentRepo = studentRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.allocationRepo = allocationRepo;
        this.feeRepo = feeRepo;
        this.complaintRepo = complaintRepo;
        this.attendanceRepo = attendanceRepo;
        this.requestRepo = requestRepo;
        this.attendanceService = attendanceService;
    }

    public List<Student> search(String q) {
        if (q == null || q.isBlank()) {
            return studentRepo.findAll(Sort.by("name"));
        }
        return studentRepo.search(q.trim());
    }

    public Student get(Long id) {
        return studentRepo.findById(id).orElseThrow(() -> ApiException.notFound("Student not found"));
    }

    public Student getByUserId(Long userId) {
        return studentRepo.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Student profile not found for this login"));
    }

    public StudentDetails details(Long id) {
        Student s = get(id);
        RoomAllocation allocation = allocationRepo.findByStudentIdAndActiveTrue(id).orElse(null);
        return new StudentDetails(s, allocation,
                feeRepo.findByStudentIdOrderByDueDateDesc(id),
                complaintRepo.findByStudentIdOrderByCreatedAtDesc(id),
                attendanceService.summary(id));
    }

    /** Creates the login (users table) and the student profile (students table) together. */
    @Transactional
    public Student create(StudentRequest r) {
        String name = Check.text(r.name(), "Name");
        String rollNo = Check.text(r.rollNo(), "Roll number");
        String username = Check.text(r.username(), "Username");
        String password = Check.text(r.password(), "Password");
        if (password.length() < 6) {
            throw ApiException.badRequest("Password must be at least 6 characters");
        }
        if (studentRepo.existsByRollNo(rollNo)) {
            throw ApiException.badRequest("Roll number already exists");
        }
        if (userRepo.existsByUsername(username)) {
            throw ApiException.badRequest("Username already taken");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setFullName(name);
        user.setRole(Role.STUDENT);
        userRepo.save(user);

        Student s = new Student();
        s.setRollNo(rollNo);
        s.setJoinDate(LocalDate.now());
        s.setUser(user);
        apply(s, r);
        return studentRepo.save(s);
    }

    @Transactional
    public Student update(Long id, StudentRequest r) {
        Student s = get(id);
        String rollNo = Check.text(r.rollNo(), "Roll number");
        if (!rollNo.equals(s.getRollNo()) && studentRepo.existsByRollNo(rollNo)) {
            throw ApiException.badRequest("Roll number already exists");
        }
        s.setRollNo(rollNo);
        apply(s, r);

        User user = s.getUser();
        if (user != null) {
            user.setFullName(s.getName());
            if (r.password() != null && !r.password().isBlank()) {
                if (r.password().length() < 6) {
                    throw ApiException.badRequest("Password must be at least 6 characters");
                }
                user.setPassword(encoder.encode(r.password()));
            }
            userRepo.save(user);
        }
        return studentRepo.save(s);
    }

    /** Deletes the student and everything linked to them (fees, complaints, ...). */
    @Transactional
    public void delete(Long id) {
        Student s = get(id);
        attendanceRepo.deleteByStudentId(id);
        requestRepo.deleteByStudentId(id);
        complaintRepo.deleteByStudentId(id);
        feeRepo.deleteByStudentId(id);
        allocationRepo.deleteByStudentId(id);
        User user = s.getUser();
        studentRepo.delete(s);
        if (user != null) {
            userRepo.delete(user);
        }
    }

    private void apply(Student s, StudentRequest r) {
        s.setName(Check.text(r.name(), "Name"));
        s.setEmail(Check.optional(r.email()));
        s.setPhone(Check.optional(r.phone()));
        s.setCourse(Check.optional(r.course()));
        s.setYearOfStudy(r.yearOfStudy());
        s.setGuardianName(Check.optional(r.guardianName()));
        s.setGuardianPhone(Check.optional(r.guardianPhone()));
        s.setAddress(Check.optional(r.address()));
    }
}
