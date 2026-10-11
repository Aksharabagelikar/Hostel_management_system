package com.hostelmanagement.service;

import com.hostelmanagement.dto.RoomView;
import com.hostelmanagement.entity.*;
import com.hostelmanagement.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/** Builds the numbers shown on dashboards and in the reports page. */
@Service
public class ReportService {

    private final StudentRepository studentRepo;
    private final RoomService roomService;
    private final RoomAllocationRepository allocationRepo;
    private final FeeRepository feeRepo;
    private final ComplaintRepository complaintRepo;
    private final RoomChangeRequestRepository requestRepo;
    private final AttendanceService attendanceService;
    private final NoticeRepository noticeRepo;

    public ReportService(StudentRepository studentRepo, RoomService roomService,
                         RoomAllocationRepository allocationRepo, FeeRepository feeRepo,
                         ComplaintRepository complaintRepo, RoomChangeRequestRepository requestRepo,
                         AttendanceService attendanceService, NoticeRepository noticeRepo) {
        this.studentRepo = studentRepo;
        this.roomService = roomService;
        this.allocationRepo = allocationRepo;
        this.feeRepo = feeRepo;
        this.complaintRepo = complaintRepo;
        this.requestRepo = requestRepo;
        this.attendanceService = attendanceService;
        this.noticeRepo = noticeRepo;
    }

    /** Cards on the admin/warden dashboard. */
    public Map<String, Object> adminDashboard() {
        List<RoomView> rooms = roomService.list("all");
        long occupied = rooms.stream().filter(r -> r.occupied() > 0).count();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalStudents", studentRepo.count());
        m.put("totalRooms", rooms.size());
        m.put("occupiedRooms", occupied);
        m.put("vacantRooms", rooms.size() - occupied);
        m.put("pendingComplaints", complaintRepo.countByStatus(ComplaintStatus.PENDING));
        m.put("inProgressComplaints", complaintRepo.countByStatus(ComplaintStatus.IN_PROGRESS));
        m.put("pendingFees", feeRepo.countByStatus(FeeStatus.PENDING));
        m.put("pendingFeeAmount", feeRepo.sumByStatus(FeeStatus.PENDING));
        m.put("pendingRoomRequests", requestRepo.countByStatus(RequestStatus.PENDING));
        m.put("unallocatedStudents", studentRepo.count() - allocationRepo.countByActiveTrue());
        m.put("totalNotices", noticeRepo.count());
        return m;
    }

    /** Data for the Reports page. */
    public Map<String, Object> reports() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("feeCollected", feeRepo.sumByStatus(FeeStatus.PAID));
        m.put("feePending", feeRepo.sumByStatus(FeeStatus.PENDING));
        m.put("feesPaidCount", feeRepo.countByStatus(FeeStatus.PAID));
        m.put("feesPendingCount", feeRepo.countByStatus(FeeStatus.PENDING));

        Map<String, Long> complaints = new LinkedHashMap<>();
        for (ComplaintStatus s : ComplaintStatus.values()) {
            complaints.put(s.name(), complaintRepo.countByStatus(s));
        }
        m.put("complaintsByStatus", complaints);

        Map<String, long[]> blocks = new TreeMap<>();
        for (RoomView r : roomService.list("all")) {
            long[] v = blocks.computeIfAbsent(r.blockName(), k -> new long[3]);
            v[0] += 1;
            v[1] += r.capacity();
            v[2] += r.occupied();
        }
        List<Map<String, Object>> occupancy = new ArrayList<>();
        for (Map.Entry<String, long[]> e : blocks.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("block", e.getKey());
            row.put("rooms", e.getValue()[0]);
            row.put("capacity", e.getValue()[1]);
            row.put("occupied", e.getValue()[2]);
            occupancy.add(row);
        }
        m.put("occupancyByBlock", occupancy);

        Map<String, Long> byCourse = studentRepo.findAll().stream().collect(Collectors.groupingBy(
                s -> (s.getCourse() == null || s.getCourse().isBlank()) ? "Not specified" : s.getCourse(),
                TreeMap::new, Collectors.counting()));
        m.put("studentsByCourse", byCourse);
        m.put("attendancePercent", attendanceService.overallPercent());
        return m;
    }

    /** Summary cards for the student dashboard. */
    public Map<String, Object> studentDashboard(Student s) {
        Map<String, Object> m = new LinkedHashMap<>();
        RoomAllocation a = allocationRepo.findByStudentIdAndActiveTrue(s.getId()).orElse(null);
        List<Fee> fees = feeRepo.findByStudentIdOrderByDueDateDesc(s.getId());
        long pending = fees.stream().filter(f -> f.getStatus() == FeeStatus.PENDING).count();
        double pendingAmount = fees.stream().filter(f -> f.getStatus() == FeeStatus.PENDING)
                .mapToDouble(Fee::getAmount).sum();

        m.put("name", s.getName());
        m.put("rollNo", s.getRollNo());
        m.put("roomNumber", a == null ? "Not allotted" : a.getRoom().getRoomNumber());
        m.put("block", a == null ? null : a.getRoom().getBlockName());
        m.put("feeStatus", fees.isEmpty() ? "NO DUES" : (pending == 0 ? "PAID" : "PENDING"));
        m.put("pendingFeeAmount", pendingAmount);
        m.put("attendancePercent", attendanceService.summary(s.getId()).percent());
        m.put("pendingComplaints", complaintRepo.countByStudentIdAndStatusNot(s.getId(), ComplaintStatus.RESOLVED));
        return m;
    }
}
