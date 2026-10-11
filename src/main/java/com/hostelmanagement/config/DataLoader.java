package com.hostelmanagement.config;

import com.hostelmanagement.entity.*;
import com.hostelmanagement.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Inserts demo data the FIRST time the application starts (only when the users table is empty).
 * Demo logins:  admin/admin123   warden/warden123   student/student123
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final RoomRepository roomRepo;
    private final RoomAllocationRepository allocationRepo;
    private final FeeRepository feeRepo;
    private final ComplaintRepository complaintRepo;
    private final AttendanceRepository attendanceRepo;
    private final NoticeRepository noticeRepo;
    private final RoomChangeRequestRepository requestRepo;
    private final PasswordEncoder encoder;

    public DataLoader(UserRepository userRepo, StudentRepository studentRepo, RoomRepository roomRepo,
                      RoomAllocationRepository allocationRepo, FeeRepository feeRepo,
                      ComplaintRepository complaintRepo, AttendanceRepository attendanceRepo,
                      NoticeRepository noticeRepo, RoomChangeRequestRepository requestRepo,
                      PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.studentRepo = studentRepo;
        this.roomRepo = roomRepo;
        this.allocationRepo = allocationRepo;
        this.feeRepo = feeRepo;
        this.complaintRepo = complaintRepo;
        this.attendanceRepo = attendanceRepo;
        this.noticeRepo = noticeRepo;
        this.requestRepo = requestRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) {
            return;
        }
        log.info("Empty database found - inserting demo data...");

        // ---------- staff users ----------
        saveUser("admin", "admin123", "Dr. Anil Kumar (Admin)", Role.ADMIN);
        saveUser("warden", "warden123", "Mr. Suresh Reddy (Warden)", Role.WARDEN);

        // ---------- rooms ----------
        String boys = "Block A (Boys)";
        String girls = "Block B (Girls)";
        Room a101 = room("A101", boys, 1, 2, "Double", 4500);
        Room a102 = room("A102", boys, 1, 2, "Double", 4500);
        Room a103 = room("A103", boys, 1, 3, "Triple", 3500);
        Room a104 = room("A104", boys, 1, 1, "Single", 6000);
        Room a201 = room("A201", boys, 2, 2, "Double", 4800);
        Room b101 = room("B101", girls, 1, 2, "Double", 4500);
        Room b102 = room("B102", girls, 1, 2, "Double", 4500);
        Room b103 = room("B103", girls, 1, 3, "Triple", 3500);
        Room b201 = room("B201", girls, 2, 1, "Single", 6000);
        Room b202 = room("B202", girls, 2, 2, "Double", 4800);

        // ---------- students (each has a login; password = student123) ----------
        List<Student> students = new ArrayList<>();
        Student rahul = student("student", "Rahul Sharma", "CS2101", "rahul.sharma@college.edu", "9876543210", "B.Tech CSE", 2, "Mr. Vijay Sharma", "9811122233", "12 MG Road, Hyderabad");
        Student priya = student("priya", "Priya Nair", "CS2102", "priya.nair@college.edu", "9876501234", "B.Tech CSE", 2, "Mrs. Lakshmi Nair", "9822233344", "45 Beach Road, Kochi");
        Student arjun = student("arjun", "Arjun Mehta", "EC2103", "arjun.mehta@college.edu", "9876512345", "B.Tech ECE", 3, "Mr. Rajesh Mehta", "9833344455", "7 Park Street, Pune");
        Student sneha = student("sneha", "Sneha Iyer", "EC2104", "sneha.iyer@college.edu", "9876523456", "B.Tech ECE", 3, "Mr. Gopal Iyer", "9844455566", "88 Temple Lane, Chennai");
        Student vikram = student("vikram", "Vikram Singh", "ME2105", "vikram.singh@college.edu", "9876534567", "B.Tech Mechanical", 1, "Mr. Harpal Singh", "9855566677", "23 Civil Lines, Jaipur");
        Student ananya = student("ananya", "Ananya Das", "CS2106", "ananya.das@college.edu", "9876545678", "B.Sc Computer Science", 1, "Mr. Subir Das", "9866677788", "9 Lake View, Kolkata");
        Student rohit = student("rohit", "Rohit Verma", "BA2107", "rohit.verma@college.edu", "9876556789", "BBA", 2, "Mr. Anil Verma", "9877788899", "56 Gandhi Nagar, Lucknow");
        Student kavya = student("kavya", "Kavya Reddy", "CS2108", "kavya.reddy@college.edu", "9876567890", "B.Tech CSE", 3, "Mr. Narayan Reddy", "9888899900", "31 Jubilee Hills, Hyderabad");
        students.addAll(List.of(rahul, priya, arjun, sneha, vikram, ananya, rohit, kavya));

        // ---------- allocations (Rohit is left without a room so you can demo "Allocate Room") ----------
        allocate(rahul, a101, 90);
        allocate(arjun, a101, 80);
        allocate(vikram, a102, 60);
        allocate(priya, b101, 90);
        allocate(sneha, b101, 85);
        allocate(ananya, b102, 50);
        allocate(kavya, b102, 70);

        // ---------- fees: last 3 months for every student ----------
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        for (int idx = 0; idx < students.size(); idx++) {
            Student s = students.get(idx);
            for (int monthsBack = 2; monthsBack >= 0; monthsBack--) {
                LocalDate due = firstOfMonth.minusMonths(monthsBack).plusDays(9);
                boolean paid;
                if (monthsBack == 2) {
                    paid = true;
                } else if (monthsBack == 1) {
                    paid = !(s == vikram || s == rohit);
                } else {
                    paid = (s == rahul || s == priya);
                }
                Fee f = new Fee();
                f.setStudent(s);
                f.setAmount(s == rohit ? 4000.0 : (s == vikram ? 4500.0 : (idx % 2 == 0 ? 4500.0 : 4800.0)));
                f.setDueDate(due);
                f.setDescription("Hostel Fee - " + due.getMonth().toString().charAt(0)
                        + due.getMonth().toString().substring(1).toLowerCase() + " " + due.getYear());
                if (paid) {
                    f.setStatus(FeeStatus.PAID);
                    f.setPaidDate(due.minusDays(3));
                    f.setPaymentMode(idx % 3 == 0 ? "UPI" : (idx % 3 == 1 ? "Cash" : "Net Banking"));
                } else {
                    f.setStatus(FeeStatus.PENDING);
                }
                feeRepo.save(f);
            }
        }

        // ---------- attendance: last 10 days ----------
        for (int idx = 0; idx < students.size(); idx++) {
            for (int day = 10; day >= 1; day--) {
                AttendanceStatus st = AttendanceStatus.PRESENT;
                if ((day + idx) % 7 == 0) {
                    st = AttendanceStatus.ABSENT;
                } else if ((day * 3 + idx) % 11 == 0) {
                    st = AttendanceStatus.LEAVE;
                }
                Attendance at = new Attendance();
                at.setStudent(students.get(idx));
                at.setAttendanceDate(LocalDate.now().minusDays(day));
                at.setStatus(st);
                attendanceRepo.save(at);
            }
        }

        // ---------- complaints ----------
        complaint(rahul, "Ceiling fan not working", "Electrical", "The fan in room A101 stopped working since Monday.", ComplaintStatus.IN_PROGRESS, 3, "Electrician has been informed.");
        complaint(rahul, "Wi-Fi is very slow", "Internet", "Internet speed drops a lot in the evening.", ComplaintStatus.PENDING, 1, null);
        complaint(priya, "Water leakage in bathroom", "Plumbing", "Tap is leaking continuously in the common bathroom.", ComplaintStatus.RESOLVED, 8, "Tap washer replaced.");
        complaint(vikram, "Broken window latch", "Maintenance", "Window latch is broken, window does not close.", ComplaintStatus.PENDING, 2, null);
        complaint(kavya, "Mess food quality", "Mess", "Food quality has gone down this week.", ComplaintStatus.PENDING, 1, null);

        // ---------- notices ----------
        notice("Hostel Rules Reminder", "Entry gates close at 9:30 PM. Visitors are allowed only in the visitors' lounge between 4 PM and 6 PM.", "Mr. Suresh Reddy (Warden)", 6, true);
        notice("Water Supply Maintenance", "Water supply will be off on Sunday from 10 AM to 2 PM due to tank cleaning. Please store water in advance.", "Dr. Anil Kumar (Admin)", 4, false);
        notice("Fee Payment Reminder", "Students with pending hostel fees must clear the dues at the warden office before the due date to avoid a late fine.", "Dr. Anil Kumar (Admin)", 2, true);
        notice("Cultural Night", "Annual hostel cultural night this Saturday at 6 PM in the main hall. All residents are invited!", "Mr. Suresh Reddy (Warden)", 1, false);

        // ---------- one pending room change request ----------
        RoomChangeRequest req = new RoomChangeRequest();
        req.setStudent(arjun);
        req.setCurrentRoom(a101);
        req.setRequestedRoom(a103);
        req.setReason("I want a larger room with space for my study table.");
        req.setStatus(RequestStatus.PENDING);
        req.setCreatedAt(LocalDateTime.now().minusDays(1));
        requestRepo.save(req);

        log.info("Demo data inserted. Logins -> admin/admin123, warden/warden123, student/student123");
    }

    // ------------------------------------------------------------------ helpers

    private User saveUser(String username, String password, String fullName, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(encoder.encode(password));
        u.setFullName(fullName);
        u.setRole(role);
        return userRepo.save(u);
    }

    private Room room(String number, String block, int floor, int capacity, String type, double rent) {
        Room r = new Room();
        r.setRoomNumber(number);
        r.setBlockName(block);
        r.setFloorNo(floor);
        r.setCapacity(capacity);
        r.setRoomType(type);
        r.setMonthlyRent(rent);
        return roomRepo.save(r);
    }

    private Student student(String username, String name, String rollNo, String email, String phone,
                            String course, int year, String guardian, String guardianPhone, String address) {
        User u = saveUser(username, "student123", name, Role.STUDENT);
        Student s = new Student();
        s.setUser(u);
        s.setName(name);
        s.setRollNo(rollNo);
        s.setEmail(email);
        s.setPhone(phone);
        s.setCourse(course);
        s.setYearOfStudy(year);
        s.setGuardianName(guardian);
        s.setGuardianPhone(guardianPhone);
        s.setAddress(address);
        s.setJoinDate(LocalDate.now().minusDays(200));
        return studentRepo.save(s);
    }

    private void allocate(Student s, Room r, int daysAgo) {
        RoomAllocation a = new RoomAllocation();
        a.setStudent(s);
        a.setRoom(r);
        a.setAllocatedDate(LocalDate.now().minusDays(daysAgo));
        a.setActive(true);
        allocationRepo.save(a);
    }

    private void complaint(Student s, String title, String category, String text, ComplaintStatus status,
                           int daysAgo, String remarks) {
        Complaint c = new Complaint();
        c.setStudent(s);
        c.setTitle(title);
        c.setCategory(category);
        c.setDescription(text);
        c.setStatus(status);
        c.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        c.setAdminRemarks(remarks);
        complaintRepo.save(c);
    }

    private void notice(String title, String content, String by, int daysAgo, boolean important) {
        Notice n = new Notice();
        n.setTitle(title);
        n.setContent(content);
        n.setPostedBy(by);
        n.setPostedDate(LocalDate.now().minusDays(daysAgo));
        n.setImportant(important);
        noticeRepo.save(n);
    }
}
