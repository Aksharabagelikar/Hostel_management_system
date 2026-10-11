package com.hostelmanagement.controller;

import com.hostelmanagement.dto.ComplaintRequest;
import com.hostelmanagement.dto.RoomChangeCreate;
import com.hostelmanagement.dto.RoomDetails;
import com.hostelmanagement.dto.RoomView;
import com.hostelmanagement.entity.*;
import com.hostelmanagement.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything the STUDENT dashboard needs. Every method works only with the data of the
 * logged-in student (found from the session), so a student can never see another student's data.
 */
@RestController
@RequestMapping("/api/student")
public class StudentPortalController {

    private final StudentService studentService;
    private final ReportService reportService;
    private final AllocationService allocationService;
    private final FeeService feeService;
    private final AttendanceService attendanceService;
    private final ComplaintService complaintService;
    private final NoticeService noticeService;
    private final RoomService roomService;
    private final RoomChangeService roomChangeService;

    public StudentPortalController(StudentService studentService, ReportService reportService,
                                   AllocationService allocationService, FeeService feeService,
                                   AttendanceService attendanceService, ComplaintService complaintService,
                                   NoticeService noticeService, RoomService roomService,
                                   RoomChangeService roomChangeService) {
        this.studentService = studentService;
        this.reportService = reportService;
        this.allocationService = allocationService;
        this.feeService = feeService;
        this.attendanceService = attendanceService;
        this.complaintService = complaintService;
        this.noticeService = noticeService;
        this.roomService = roomService;
        this.roomChangeService = roomChangeService;
    }

    private Student me(HttpSession session) {
        return studentService.getByUserId((Long) session.getAttribute("USER_ID"));
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(HttpSession session) {
        return reportService.studentDashboard(me(session));
    }

    @GetMapping("/profile")
    public Student profile(HttpSession session) {
        return me(session);
    }

    @GetMapping("/room")
    public RoomDetails room(HttpSession session) {
        return allocationService.roomDetails(me(session).getId());
    }

    @GetMapping("/fees")
    public Map<String, Object> fees(HttpSession session) {
        List<Fee> fees = feeService.forStudent(me(session).getId());
        double paid = fees.stream().filter(f -> f.getStatus() == FeeStatus.PAID).mapToDouble(Fee::getAmount).sum();
        double pending = fees.stream().filter(f -> f.getStatus() == FeeStatus.PENDING).mapToDouble(Fee::getAmount).sum();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("fees", fees);
        m.put("totalPaid", paid);
        m.put("totalPending", pending);
        return m;
    }

    @GetMapping("/attendance")
    public Map<String, Object> attendance(HttpSession session) {
        Long id = me(session).getId();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("summary", attendanceService.summary(id));
        m.put("records", attendanceService.history(id));
        return m;
    }

    @GetMapping("/complaints")
    public List<Complaint> complaints(HttpSession session) {
        return complaintService.forStudent(me(session).getId());
    }

    @PostMapping("/complaints")
    public Complaint submitComplaint(@RequestBody ComplaintRequest request, HttpSession session) {
        return complaintService.create(me(session), request);
    }

    @GetMapping("/notices")
    public List<Notice> notices() {
        return noticeService.listAll();
    }

    @GetMapping("/available-rooms")
    public List<RoomView> availableRooms() {
        return roomService.list("available");
    }

    @GetMapping("/room-requests")
    public List<RoomChangeRequest> roomRequests(HttpSession session) {
        return roomChangeService.forStudent(me(session).getId());
    }

    @PostMapping("/room-requests")
    public RoomChangeRequest createRoomRequest(@RequestBody RoomChangeCreate request, HttpSession session) {
        return roomChangeService.create(me(session), request);
    }
}
